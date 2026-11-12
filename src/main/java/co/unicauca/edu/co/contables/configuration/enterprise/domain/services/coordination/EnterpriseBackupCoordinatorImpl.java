package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

import co.unicauca.edu.co.contables.commons.backup.BackupCorruptedException;
import co.unicauca.edu.co.contables.commons.backup.BackupException;
import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.InMemoryIdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.commons.backup.UnsupportedBackupVersionException;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupWriter;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseNitAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.PurgeNotSupportedException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.UnsupportedBackupModuleException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Coordinador de la copia de seguridad completa (ver
 * {@link IEnterpriseBackupCoordinator}).
 *
 * Descubre los participantes por inyección ({@code List<BackupParticipant>},
 * sin lista manual): agregar un módulo = agregar un bean. En el constructor
 * (arranque) valida claves repetidas, dependencias faltantes, ciclos y que
 * {@code order()} respete {@code requires()}; si algo está mal la aplicación
 * no arranca.
 *
 * Transacciones programáticas ({@link TransactionTemplate}) para que el orden
 * sea explícito y no dependa de proxies: verificación del ZIP FUERA de la
 * transacción, todos los restores + verificación final DENTRO de una sola.
 */
@Service
@Slf4j
public class EnterpriseBackupCoordinatorImpl implements IEnterpriseBackupCoordinator {

    private static final int MAX_PROBLEMS_IN_MESSAGE = 10;

    private final List<BackupParticipant> participants;
    private final Map<String, BackupParticipant> byKey;
    private final ZipBackupFormat format;
    private final EnterpriseRepository enterpriseRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate restoreTx;
    private final TransactionTemplate exportTx;
    private final String appVersion;
    private final Path workDir;

    public EnterpriseBackupCoordinatorImpl(List<BackupParticipant> participants, ZipBackupFormat format,
            EnterpriseRepository enterpriseRepository, EntityManager entityManager,
            PlatformTransactionManager transactionManager,
            @Value("${spring.application.name:ms-configuration-rf}") String appName,
            @Value("${app.backup.dir:uploads/enterprises/backups}") String backupDir) {
        this.participants = validateAndSort(participants);
        Map<String, BackupParticipant> map = new LinkedHashMap<>();
        this.participants.forEach(p -> map.put(p.moduleKey(), p));
        this.byKey = Map.copyOf(map);
        this.format = format;
        this.enterpriseRepository = enterpriseRepository;
        this.entityManager = entityManager;
        this.restoreTx = new TransactionTemplate(transactionManager);
        this.restoreTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.exportTx = new TransactionTemplate(transactionManager);
        this.exportTx.setReadOnly(true);
        this.appVersion = appName;
        this.workDir = Paths.get(backupDir).toAbsolutePath().normalize().resolve("tmp");
        log.info("Copia de seguridad: participantes {}", this.participants.stream()
                .map(p -> p.moduleKey() + "(" + p.order() + ")").toList());
    }

    /* ------------------------------------------------------------------ */
    /* Grafo de participantes                                             */
    /* ------------------------------------------------------------------ */

    /**
     * Valida el grafo y devuelve los participantes ordenados por
     * {@code order()} (empate: {@code moduleKey}).
     * @throws IllegalStateException si el grafo es inválido
     */
    static List<BackupParticipant> validateAndSort(List<BackupParticipant> input) {
        List<BackupParticipant> list = input == null ? List.of() : input.stream().filter(Objects::nonNull).toList();
        Map<String, BackupParticipant> keys = new HashMap<>();
        for (BackupParticipant p : list) {
            String key = p.moduleKey();
            if (key == null || key.isBlank()) {
                throw new IllegalStateException("El participante " + p.getClass().getName() + " no declara moduleKey");
            }
            if (p.schemaVersion() < 1) {
                throw new IllegalStateException("El participante '" + key + "' declara schemaVersion < 1");
            }
            BackupParticipant previous = keys.putIfAbsent(key, p);
            if (previous != null) {
                throw new IllegalStateException("moduleKey repetido en la copia de seguridad: '" + key + "' ("
                        + previous.getClass().getName() + " y " + p.getClass().getName() + ")");
            }
        }
        BackupParticipant enterprise = keys.get(BackupKeys.MODULE_ENTERPRISE);
        if (enterprise == null) {
            throw new IllegalStateException("Falta el participante '" + BackupKeys.MODULE_ENTERPRISE
                    + "' de la copia de seguridad");
        }
        for (BackupParticipant p : list) {
            Set<String> requires = p.requires() == null ? Set.of() : p.requires();
            for (String dep : requires) {
                if (p.moduleKey().equals(dep)) {
                    throw new IllegalStateException("El participante '" + p.moduleKey() + "' depende de sí mismo");
                }
                BackupParticipant target = keys.get(dep);
                if (target == null) {
                    throw new IllegalStateException("El participante '" + p.moduleKey()
                            + "' requiere el módulo '" + dep + "', que no existe");
                }
            }
            if (p != enterprise && p.order() <= enterprise.order()) {
                throw new IllegalStateException("El participante '" + p.moduleKey() + "' (order " + p.order()
                        + ") debe ejecutarse después de '" + BackupKeys.MODULE_ENTERPRISE + "' (order "
                        + enterprise.order() + "): es quien crea la empresa destino");
            }
        }
        detectCycles(list, keys);
        for (BackupParticipant p : list) {
            Set<String> requires = p.requires() == null ? Set.of() : p.requires();
            for (String dep : requires) {
                BackupParticipant target = keys.get(dep);
                if (target.order() >= p.order()) {
                    throw new IllegalStateException("El participante '" + p.moduleKey() + "' (order " + p.order()
                            + ") requiere '" + dep + "' (order " + target.order()
                            + "): su order debe ser mayor que el de sus dependencias");
                }
            }
        }
        List<BackupParticipant> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(BackupParticipant::order).thenComparing(BackupParticipant::moduleKey));
        return List.copyOf(sorted);
    }

    private static void detectCycles(List<BackupParticipant> list, Map<String, BackupParticipant> keys) {
        Set<String> done = new HashSet<>();
        for (BackupParticipant p : list) {
            visit(p.moduleKey(), keys, done, new LinkedHashSet<>());
        }
    }

    private static void visit(String key, Map<String, BackupParticipant> keys, Set<String> done,
            LinkedHashSet<String> path) {
        if (done.contains(key)) {
            return;
        }
        if (!path.add(key)) {
            List<String> cycle = new ArrayList<>(path);
            cycle = cycle.subList(cycle.indexOf(key), cycle.size());
            throw new IllegalStateException("Ciclo de dependencias entre participantes de la copia: "
                    + String.join(" -> ", cycle) + " -> " + key);
        }
        BackupParticipant p = keys.get(key);
        Set<String> requires = p.requires() == null ? Set.of() : p.requires();
        for (String dep : requires) {
            visit(dep, keys, done, path);
        }
        path.remove(key);
        done.add(key);
    }

    @Override
    public List<BackupModuleInfo> supportedModules() {
        return participants.stream()
                .map(p -> new BackupModuleInfo(p.moduleKey(), p.order(), p.schemaVersion(),
                        p.requires() == null ? List.of() : p.requires().stream().sorted().toList()))
                .toList();
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public ManifestV2 exportTo(UUID enterpriseId, Path target, String username, BackupProgressListener listener) {
        if (enterpriseId == null || target == null) {
            throw new InvalidRestoreRequestException("Debe indicar la empresa y el archivo de destino del backup");
        }
        BackupProgressListener progress = BackupProgressListener.orNone(listener);
        return exportTx.execute(status -> {
            // findById está filtrado por @TenantId: solo se exportan empresas del tenant actual.
            EnterpriseEntity enterprise = enterpriseRepository.findById(enterpriseId)
                    .orElseThrow(() -> new EnterpriseNotFoundException(enterpriseId));
            ZipBackupWriter.Header header = new ZipBackupWriter.Header(enterpriseId, enterprise.getName(),
                    appVersion, ManifestV2.CreatedBy.of(TenantContext.getTenantId(), username),
                    BackupKeys.EXCLUDED, LocalDateTime.now());
            progress.onStage(BackupProgressListener.Stage.EXPORT, participants.size());
            try (ZipBackupWriter zip = format.newWriter(target, header)) {
                for (BackupParticipant p : participants) {
                    progress.onModuleStarted(BackupProgressListener.Stage.EXPORT, p.moduleKey());
                    try {
                        p.export(enterpriseId, zip.module(p.moduleKey(), p.schemaVersion()));
                    } catch (RuntimeException e) {
                        throw wrapExport(p.moduleKey(), e);
                    }
                    progress.onModuleCompleted(BackupProgressListener.Stage.EXPORT, p.moduleKey());
                }
                return zip.finish();
            }
        });
    }

    private RuntimeException wrapExport(String moduleKey, RuntimeException e) {
        if (e instanceof BackupException || e instanceof EnterpriseNotFoundException) {
            return e;
        }
        log.error("Error exportando el módulo '{}' de la copia", moduleKey, e);
        return new IllegalStateException("Error exportando el módulo '" + moduleKey + "': "
                + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()), e);
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public PreparedRestore prepare(Path zip, RestoreOptions options, RestoreActor actor) {
        if (zip == null || options == null || actor == null) {
            throw new InvalidRestoreRequestException("Debe indicar el archivo, las opciones y el usuario del restore");
        }
        boolean inPlace = options.mode() == RestoreMode.IN_PLACE;
        if (inPlace) {
            if (options.targetEnterpriseId() == null) {
                throw new InvalidRestoreRequestException(
                        "Restaurar sobre una empresa existente requiere indicar la empresa destino");
            }
            // existsById está filtrado por @TenantId: una empresa de otro tenant se ve como inexistente (404).
            if (!enterpriseRepository.existsById(options.targetEnterpriseId())) {
                throw new EnterpriseNotFoundException(options.targetEnterpriseId());
            }
        }
        ZipBackupReader reader = format.open(zip);
        try {
            if (inPlace && reader.formatVersion() < 2) {
                throw new InvalidRestoreRequestException("Las copias de formato " + reader.formatVersion()
                        + " solo traen los datos de la empresa y no se restauran con el flujo de copia completa");
            }
            // Checksums y tamaños de TODO antes de abrir la transacción: un ZIP manipulado no toca la BD.
            reader.verifyAll();
            ManifestV2 manifest = reader.manifest();
            Set<String> zipModules = reader.moduleKeys();
            if (!zipModules.contains(BackupKeys.MODULE_ENTERPRISE)) {
                throw new BackupCorruptedException("El archivo no es una copia válida: no incluye los datos de la "
                        + "empresa (módulo '" + BackupKeys.MODULE_ENTERPRISE + "')");
            }
            List<String> unknown = zipModules.stream().filter(k -> !byKey.containsKey(k)).toList();
            if (!unknown.isEmpty() && !options.skipUnknownModules()) {
                throw new UnsupportedBackupModuleException(unknown);
            }
            if (!unknown.isEmpty()) {
                log.warn("Restore: se omiten los módulos desconocidos {}", unknown);
            }
            List<BackupParticipant> plan = participants.stream()
                    .filter(p -> zipModules.contains(p.moduleKey())).toList();
            for (BackupParticipant p : plan) {
                ManifestV2.ModuleEntry module = manifest.module(p.moduleKey()).orElseThrow();
                if (module.schema() > p.schemaVersion()) {
                    throw new UnsupportedBackupVersionException("La copia trae el módulo '" + p.moduleKey()
                            + "' con formato " + module.schema() + " y esta versión lee hasta el formato "
                            + p.schemaVersion() + ". Actualiza la aplicación para restaurarla.");
                }
                for (String dep : p.requires() == null ? Set.<String>of() : p.requires()) {
                    if (!zipModules.contains(dep)) {
                        throw new BackupCorruptedException("El archivo no es una copia válida: incluye el módulo '"
                                + p.moduleKey() + "' pero no '" + dep + "', del que depende");
                    }
                }
            }
            if (inPlace) {
                // Falla ANTES de tocar la BD: sin proceso fallido y sin datos a medias.
                for (BackupParticipant p : plan) {
                    if (!supportsPurge(p)) {
                        throw new PurgeNotSupportedException(p.moduleKey());
                    }
                }
            }
            RestoreContext ctx = initialContext(reader, manifest, options, actor);
            for (BackupParticipant p : plan) {
                try {
                    p.preflight(reader.module(p.moduleKey()), ctx);
                } catch (BackupIntegrityException e) {
                    throw e.inModule(p.moduleKey());
                }
            }
            return new PreparedRestore(reader, ctx, plan, unknown);
        } catch (RuntimeException e) {
            reader.close();
            throw e;
        }
    }

    private RestoreContext initialContext(ZipBackupReader reader, ManifestV2 manifest, RestoreOptions options,
            RestoreActor actor) {
        // Clave del IdMap para la empresa: nunca null (ZIP v1 viejos no declaran el id de origen).
        UUID source = manifest.sourceEnterpriseId() != null ? manifest.sourceEnterpriseId() : UUID.randomUUID();
        String name = options.targetName();
        if (name == null && options.mode() == RestoreMode.DUPLICATE && manifest.sourceEnterpriseName() != null) {
            name = manifest.sourceEnterpriseName() + " (copia)";
        }
        // tenant y usuario de la petición actual (RestoreActor), NUNCA los del ZIP.
        // IN_PLACE: la empresa destino ya existe, los participantes la ven desde el preflight.
        return new RestoreContext(actor.tenantId(), actor.userId(), source, options.targetEnterpriseId(),
                reader.formatVersion(),
                manifest.exportedAt(), options.mode(), name, options.targetNit(), options.skipUnknownModules());
    }

    @Override
    public RestoreResult execute(PreparedRestore prepared, BackupProgressListener listener) {
        if (prepared == null) {
            throw new InvalidRestoreRequestException("No hay un restore preparado");
        }
        prepared.markExecuted();
        BackupProgressListener progress = BackupProgressListener.orNone(listener);
        try {
            return restoreTx.execute(status -> runInTransaction(prepared, progress));
        } catch (DataIntegrityViolationException | PersistenceException e) {
            // Violación que recién aparece en el commit (ya hubo rollback).
            throw new RestoreFailedException("Error confirmando el restore: " + BackupErrorMessages.integrityMessage(e),
                    BackupErrorMessages.integrityUserMessage(null, e), e);
        }
    }

    private RestoreResult runInTransaction(PreparedRestore prepared, BackupProgressListener progress) {
        ZipBackupReader reader = prepared.reader();
        ManifestV2 manifest = reader.manifest();
        IdMap idMap = new InMemoryIdMap();
        RestoreContext ctx = prepared.context();
        List<BackupParticipant> plan = prepared.plan();
        List<String> restored = new ArrayList<>();

        if (ctx.mode() == RestoreMode.IN_PLACE) {
            // La empresa ZIP se equivale a la existente; el participante enterprise la actualiza, no la crea.
            idMap.put(BackupKeys.ENTERPRISE, ctx.sourceEnterpriseId(), ctx.requireTargetEnterpriseId());
            purgeAll(plan, ctx.requireTargetEnterpriseId());
        }

        progress.onStage(BackupProgressListener.Stage.RESTORE, plan.size());
        for (BackupParticipant p : plan) {
            String key = p.moduleKey();
            progress.onModuleStarted(BackupProgressListener.Stage.RESTORE, key);
            try {
                p.restore(reader.module(key), idMap, ctx);
                // flush por módulo: una violación de BD sale atribuida a ESTE módulo y no al commit.
                entityManager.flush();
            } catch (RuntimeException e) {
                throw wrapRestore(key, e);
            }
            if (ctx.targetEnterpriseId() == null && BackupKeys.MODULE_ENTERPRISE.equals(key)) {
                ctx = ctx.withTargetEnterpriseId(createdEnterprise(idMap, ctx));
            }
            restored.add(key);
            progress.onModuleCompleted(BackupProgressListener.Stage.RESTORE, key);
        }
        UUID target = ctx.targetEnterpriseId();
        if (target == null) {
            throw new BackupIntegrityException(BackupKeys.MODULE_ENTERPRISE, "no se creó la empresa destino");
        }

        progress.onStage(BackupProgressListener.Stage.VERIFY, plan.size());
        Map<String, Long> counts = new LinkedHashMap<>();
        for (BackupParticipant p : plan) {
            progress.onModuleStarted(BackupProgressListener.Stage.VERIFY, p.moduleKey());
            verifyModule(p, manifest, target, counts);
            progress.onModuleCompleted(BackupProgressListener.Stage.VERIFY, p.moduleKey());
        }
        log.info("Restore confirmado: empresa {} (copia v{}), módulos {}", target, reader.formatVersion(), restored);
        return new RestoreResult(target, reader.formatVersion(), restored, prepared.skippedModules(), counts);
    }

    /**
     * IN_PLACE: borra los datos actuales de la empresa en orden INVERSO de
     * {@code order()} (primero los dependientes). Solo purga los módulos que
     * trae la copia (los que se van a restaurar). Un módulo sin soporte de purga
     * ({@link UnsupportedOperationException}) aborta todo con rollback.
     */
    private void purgeAll(List<BackupParticipant> plan, UUID enterpriseId) {
        List<BackupParticipant> reversed = new ArrayList<>(plan);
        java.util.Collections.reverse(reversed);
        for (BackupParticipant p : reversed) {
            try {
                p.purge(enterpriseId);
                // Los deletes llegan a la BD ahora: una violación sale atribuida a ESTE módulo.
                entityManager.flush();
            } catch (UnsupportedOperationException e) {
                throw new PurgeNotSupportedException(p.moduleKey());
            } catch (RuntimeException e) {
                throw wrapRestore(p.moduleKey(), e);
            }
        }
        // Las entidades cargadas antes de la purga ya no reflejan la BD.
        entityManager.clear();
        log.info("Restore en el lugar: purgados {} módulos de la empresa {}", reversed.size(), enterpriseId);
    }

    /** {@code true} si el participante sobrescribe el {@code purge} por defecto de la interfaz. */
    private static boolean supportsPurge(BackupParticipant p) {
        try {
            return ClassUtils.getUserClass(p).getMethod("purge", UUID.class)
                    .getDeclaringClass() != BackupParticipant.class;
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    private UUID createdEnterprise(IdMap idMap, RestoreContext ctx) {
        if (!idMap.contains(BackupKeys.ENTERPRISE, ctx.sourceEnterpriseId())) {
            throw new BackupIntegrityException(BackupKeys.MODULE_ENTERPRISE,
                    "el módulo no registró la empresa creada (IdMap " + BackupKeys.ENTERPRISE + ")");
        }
        return idMap.requireUuid(BackupKeys.ENTERPRISE, ctx.sourceEnterpriseId());
    }

    private void verifyModule(BackupParticipant p, ManifestV2 manifest, UUID target, Map<String, Long> counts) {
        String key = p.moduleKey();
        List<String> problems;
        Map<String, Long> actual;
        try {
            problems = p.verifyIntegrity(target);
            actual = p.count(target);
        } catch (RuntimeException e) {
            throw wrapRestore(key, e);
        }
        if (problems != null && !problems.isEmpty()) {
            List<String> shown = problems.size() > MAX_PROBLEMS_IN_MESSAGE
                    ? problems.subList(0, MAX_PROBLEMS_IN_MESSAGE) : problems;
            String more = problems.size() > shown.size() ? " (y " + (problems.size() - shown.size()) + " más)" : "";
            throw new BackupIntegrityException(key, String.join("; ", shown) + more);
        }
        if (actual == null) {
            return;
        }
        ManifestV2.ModuleEntry module = manifest.module(key).orElseThrow();
        for (ManifestV2.EntryInfo entry : module.entries()) {
            Long inDb = actual.get(entry.path());
            if (inDb != null && entry.count() != null && !inDb.equals(entry.count())) {
                throw new BackupIntegrityException(key, "'" + entry.path() + "' tiene " + entry.count()
                        + " registros en la copia y " + inDb + " en la base de datos");
            }
        }
        counts.putAll(actual);
    }

    private RuntimeException wrapRestore(String moduleKey, RuntimeException e) {
        if (e instanceof BackupIntegrityException integrity) {
            return integrity.inModule(moduleKey);
        }
        if (e instanceof BackupException || e instanceof EnterpriseNitAlreadyExistsException
                || e instanceof InvalidRestoreRequestException || e instanceof RestoreFailedException) {
            return e;
        }
        if (e instanceof DataIntegrityViolationException || e instanceof PersistenceException) {
            return new RestoreFailedException("Error restaurando '" + moduleKey + "': "
                    + BackupErrorMessages.integrityMessage(e), BackupErrorMessages.integrityUserMessage(moduleKey, e), e);
        }
        log.error("Error restaurando el módulo '{}'", moduleKey, e);
        String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        String user = e instanceof IllegalArgumentException
                ? "No se pudo restaurar el módulo '" + moduleKey + "': " + detail
                : "No se pudo restaurar el módulo '" + moduleKey + "': error inesperado. Revisa que la copia sea válida.";
        return new RestoreFailedException("Error restaurando '" + moduleKey + "': " + detail, user, e);
    }

    /* ------------------------------------------------------------------ */
    /* Duplicate                                                          */
    /* ------------------------------------------------------------------ */

    @Override
    public RestoreResult duplicate(UUID enterpriseId, String targetName, RestoreActor actor, String username,
            BackupProgressListener listener) {
        Path tmp;
        try {
            Files.createDirectories(workDir);
            tmp = workDir.resolve("duplicate-" + UUID.randomUUID() + ".zip");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo preparar el directorio temporal de la copia", e);
        }
        try {
            exportTo(enterpriseId, tmp, username, listener);
            return restore(tmp, RestoreOptions.duplicate(targetName), actor, listener);
        } finally {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException e) {
                log.warn("No se pudo borrar el ZIP temporal de duplicar '{}'", tmp, e);
            }
        }
    }
}
