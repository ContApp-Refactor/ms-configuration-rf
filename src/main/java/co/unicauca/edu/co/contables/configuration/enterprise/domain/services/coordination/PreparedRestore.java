package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;

/**
 * @brief Restore validado y listo para ejecutar (resultado de
 * {@link IEnterpriseBackupCoordinator#prepare}).
 *
 * Ya pasó, SIN tocar la BD: estructura del ZIP, checksums de todas las
 * entradas ({@code verifyAll}), versión/schema de cada módulo, módulos
 * desconocidos, dependencias entre módulos y los {@code preflight} de los
 * participantes (p. ej. NIT repetido). Mantiene el ZIP abierto: hay que
 * cerrarlo (try-with-resources). Se ejecuta una sola vez.
 */
public final class PreparedRestore implements AutoCloseable {

    private final ZipBackupReader reader;
    private final RestoreContext context;
    private final List<BackupParticipant> plan;
    private final List<String> skippedModules;
    private boolean executed;

    PreparedRestore(ZipBackupReader reader, RestoreContext context, List<BackupParticipant> plan,
            List<String> skippedModules) {
        this.reader = reader;
        this.context = context;
        this.plan = List.copyOf(plan);
        this.skippedModules = List.copyOf(skippedModules);
    }

    public int formatVersion() {
        return reader.formatVersion();
    }

    public ManifestV2 manifest() {
        return reader.manifest();
    }

    /** Empresa de origen según el manifest ({@code null} si un ZIP v1 viejo no la declaraba). */
    public UUID sourceEnterpriseId() {
        return reader.manifest().sourceEnterpriseId();
    }

    public LocalDateTime exportedAt() {
        return reader.manifest().exportedAt();
    }

    /** Contexto inicial (sin empresa destino todavía). */
    public RestoreContext context() {
        return context;
    }

    /** Módulos que se restaurarán, en orden. */
    public List<String> moduleKeys() {
        return plan.stream().map(BackupParticipant::moduleKey).toList();
    }

    public List<String> skippedModules() {
        return skippedModules;
    }

    ZipBackupReader reader() {
        return reader;
    }

    List<BackupParticipant> plan() {
        return plan;
    }

    void markExecuted() {
        if (executed) {
            throw new IllegalStateException("Este restore ya se ejecutó");
        }
        executed = true;
    }

    @Override
    public void close() {
        reader.close();
    }
}
