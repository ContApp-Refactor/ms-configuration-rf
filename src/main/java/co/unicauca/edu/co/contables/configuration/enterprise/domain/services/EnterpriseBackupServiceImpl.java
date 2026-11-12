package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.NdjsonSupport;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.backup.EnterpriseSnapshotSupport;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupProgressListener;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.IEnterpriseBackupCoordinator;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupTooLargeException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidBackupFileException;
import lombok.RequiredArgsConstructor;

/**
 * @brief Archivos de backup de empresas: el contenido (ZIP v2, todos los
 * módulos) lo arma {@link IEnterpriseBackupCoordinator}; aquí se decide la
 * ruta por tenant, se guardan los uploads con {@code transferTo} (sin
 * {@code getBytes()}) y se resuelven ids sin path traversal.
 */
@Service
@RequiredArgsConstructor
public class EnterpriseBackupServiceImpl implements IEnterpriseBackupService {

    /** Ids generados por este servicio (UUID); cualquier otra cosa es "no existe". */
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9_-]{1,100}$");
    private static final String NO_TENANT_DIR = "_sin-tenant";

    private final IEnterpriseBackupCoordinator coordinator;
    private final ZipBackupFormat format;

    @Value("${app.backup.dir:uploads/enterprises/backups}")
    private String backupDir;

    @Value("${app.backup.max-size-bytes:524288000}")
    private long maxSizeBytes;

    @Override
    public BackupReferenceDto backup(UUID enterpriseId, String username, BackupProgressListener listener) {
        String backupId = UUID.randomUUID().toString();
        Path target = tenantDir().resolve(backupId + ".zip");
        ManifestV2 manifest = coordinator.exportTo(enterpriseId, target, username, listener);
        return BackupReferenceDto.builder()
                .backupId(backupId)
                .sourceEnterpriseId(enterpriseId)
                .fileName(backupId + ".zip")
                .sizeBytes(sizeOf(target))
                .createdAt(manifest.exportedAt())
                .build();
    }

    @Override
    public BackupReferenceDto storeUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidBackupFileException("El archivo de backup subido está vacío");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BackupTooLargeException(file.getSize(), maxSizeBytes);
        }
        String backupId = UUID.randomUUID().toString();
        Path target = tenantDir().resolve(backupId + ".zip");
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            deleteQuietly(target);
            throw new InvalidBackupFileException("No se pudo guardar el archivo de backup subido", e);
        }
        return BackupReferenceDto.builder()
                .backupId(backupId)
                .fileName(backupId + ".zip")
                .sizeBytes(sizeOf(target))
                .build();
    }

    @Override
    public Path resolveBackupFile(String backupId) {
        if (backupId == null || !SAFE_ID.matcher(backupId).matches()) {
            throw new BackupNotFoundException(backupId);
        }
        for (Path candidate : new Path[] { safeResolve(tenantDir(), backupId), safeResolve(rootDir(), backupId) }) {
            if (candidate != null && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        throw new BackupNotFoundException(backupId);
    }

    @Override
    public Resource loadBackupFile(String backupId) {
        Path file = resolveBackupFile(backupId);
        Resource resource = new FileSystemResource(file);
        if (!resource.isReadable()) {
            throw new BackupNotFoundException(backupId);
        }
        return resource;
    }

    @Override
    public BackupManifest readManifest(String backupId) {
        return readManifest(resolveBackupFile(backupId));
    }

    @Override
    public BackupManifest readManifest(Path zip) {
        try (ZipBackupReader reader = format.open(zip)) {
            if (!reader.hasModule(BackupKeys.MODULE_ENTERPRISE)) {
                throw new InvalidBackupFileException("El archivo de backup no contiene los datos de la empresa");
            }
            return EnterpriseSnapshotSupport.readPayload(reader.module(BackupKeys.MODULE_ENTERPRISE));
        }
    }

    @Override
    public void deleteBackup(String backupId) {
        Path file;
        try {
            file = resolveBackupFile(backupId);
        } catch (BackupNotFoundException e) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el backup '" + backupId + "'", e);
        }
    }

    private Path rootDir() {
        return Paths.get(backupDir).toAbsolutePath().normalize();
    }

    /** {@code app.backup.dir/<sha256(tenant)>}: el tenant no aparece en claro en el disco. */
    private Path tenantDir() {
        String tenant = TenantContext.getTenantId();
        String name = tenant == null || tenant.isBlank() ? NO_TENANT_DIR : NdjsonSupport.sha256Hex(tenant);
        return rootDir().resolve(name);
    }

    /** Resuelve {@code <dir>/<id>.zip} y verifica que siga dentro de {@code dir} (anti path traversal). */
    private static Path safeResolve(Path dir, String backupId) {
        Path target = dir.resolve(backupId + ".zip").normalize();
        return target.startsWith(dir) ? target : null;
    }

    private static long sizeOf(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return 0L;
        }
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // mejor esfuerzo
        }
    }
}
