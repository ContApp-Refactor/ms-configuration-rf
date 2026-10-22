package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.EnterpriseSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.LocationSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.PersonTypeSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.SubjectSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupTooLargeException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidBackupFileException;
import lombok.RequiredArgsConstructor;

/**
 * @brief Implementación de la copia de seguridad (export) de una empresa.
 *
 * El backup es un ZIP con una sola entrada, {@code enterprise.json}, con el
 * contenido de {@link BackupManifest}. Las materias (subjects) se exportan
 * tal cual las ve el tenant actual (son datos del tenant, no de una empresa
 * puntual: ver {@link BackupManifest}).
 */
@Service
@RequiredArgsConstructor
public class EnterpriseBackupServiceImpl implements IEnterpriseBackupService {

    private static final String MANIFEST_ENTRY = "enterprise.json";

    private final EnterpriseRepository enterpriseRepository;
    private final SubjectRepository subjectRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.backup.dir:uploads/enterprises/backups}")
    private String backupDir;

    @Value("${app.backup.max-size-bytes:524288000}")
    private long maxSizeBytes;

    @Override
    @Transactional(readOnly = true)
    public BackupReferenceDto backup(UUID enterpriseId) {
        EnterpriseEntity entity = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new EnterpriseNotFoundException(enterpriseId));

        BackupManifest manifest = toManifest(entity);
        byte[] zipBytes = zip(manifest);

        if (zipBytes.length > maxSizeBytes) {
            throw new BackupTooLargeException(zipBytes.length, maxSizeBytes);
        }

        String backupId = UUID.randomUUID().toString();
        try {
            Path dir = Paths.get(backupDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(backupId + ".zip");
            Files.write(target, zipBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el backup", e);
        }

        return BackupReferenceDto.builder()
                .backupId(backupId)
                .sourceEnterpriseId(enterpriseId)
                .fileName(backupId + ".zip")
                .sizeBytes(zipBytes.length)
                .createdAt(manifest.getExportedAt())
                .build();
    }

    @Override
    public Resource loadBackupFile(String backupId) {
        Path target = resolveBackupPath(backupId);
        try {
            Resource resource = new UrlResource(target.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BackupNotFoundException(backupId);
        } catch (MalformedURLException e) {
            throw new BackupNotFoundException(backupId);
        }
    }

    @Override
    public BackupManifest readManifest(String backupId) {
        Path target = resolveBackupPath(backupId);
        if (!Files.exists(target)) {
            throw new BackupNotFoundException(backupId);
        }
        try {
            return readManifest(Files.readAllBytes(target));
        } catch (IOException e) {
            throw new InvalidBackupFileException("No se pudo leer el backup '" + backupId + "'", e);
        }
    }

    @Override
    public BackupManifest readManifest(byte[] zipBytes) {
        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (MANIFEST_ENTRY.equals(entry.getName())) {
                    return objectMapper.readValue(zipIn.readAllBytes(), BackupManifest.class);
                }
            }
        } catch (IOException e) {
            throw new InvalidBackupFileException("El archivo de backup está corrupto o no es un ZIP válido", e);
        }
        throw new InvalidBackupFileException(
                "El archivo de backup no contiene '" + MANIFEST_ENTRY + "'");
    }

    @Override
    public void deleteBackup(String backupId) {
        Path target = resolveBackupPath(backupId);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el backup '" + backupId + "'", e);
        }
    }

    /**
     * Resuelve {@code backupId} contra {@code app.backup.dir} y verifica que
     * el resultado normalizado siga dentro de ese directorio: evita path
     * traversal (p. ej. {@code backupId = "../../etc/passwd"}).
     */
    private Path resolveBackupPath(String backupId) {
        if (backupId == null || backupId.isBlank()) {
            throw new BackupNotFoundException(backupId);
        }
        Path dir = Paths.get(backupDir).toAbsolutePath().normalize();
        Path target = dir.resolve(backupId + ".zip").normalize();
        if (!target.startsWith(dir)) {
            throw new BackupNotFoundException(backupId);
        }
        return target;
    }

    private byte[] zip(BackupManifest manifest) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            byte[] json = objectMapper.writeValueAsBytes(manifest);
            zipOut.putNextEntry(new ZipEntry(MANIFEST_ENTRY));
            zipOut.write(json);
            zipOut.closeEntry();
            zipOut.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al empaquetar el backup", e);
        }
    }

    private BackupManifest toManifest(EnterpriseEntity entity) {
        List<Long> taxLiabilityIds = entity.getTaxLiabilities() == null ? List.of()
                : entity.getTaxLiabilities().stream().map(TaxLiabilityEntity::getId).toList();

        PersonTypeSnapshot personTypeSnapshot = entity.getPersonType() == null ? null
                : PersonTypeSnapshot.builder()
                        .type(entity.getPersonType().getType())
                        .name(entity.getPersonType().getName())
                        .surname(entity.getPersonType().getSurname())
                        .bussinessName(entity.getPersonType().getBussinessName())
                        .build();

        LocationSnapshot locationSnapshot = entity.getLocation() == null ? null
                : LocationSnapshot.builder()
                        .address(entity.getLocation().getAddress())
                        .cityId(entity.getLocation().getCity() == null ? null
                                : entity.getLocation().getCity().getId())
                        .departmentId(entity.getLocation().getDepartment() == null ? null
                                : entity.getLocation().getDepartment().getId())
                        .countryId(entity.getLocation().getCountry() == null ? null
                                : entity.getLocation().getCountry().getId())
                        .build();

        EnterpriseSnapshot enterpriseSnapshot = EnterpriseSnapshot.builder()
                .idUser(entity.getIdUser())
                .name(entity.getName())
                .nit(entity.getNit())
                .DV(entity.getDV())
                .phone(entity.getPhone())
                .branch(entity.getBranch())
                .email(entity.getEmail())
                .logo(entity.getLogo())
                .mainActivity(entity.getMainActivity())
                .secondaryActivity(entity.getSecondaryActivity())
                .state(entity.getState())
                .inventoryConfigurationType(entity.getInventoryConfigurationType())
                .taxLiabilityIds(taxLiabilityIds)
                .taxPayerTypeId(entity.getTaxPayerType() == null ? null : entity.getTaxPayerType().getId())
                .enterpriseTypeId(entity.getEnterpriseType() == null ? null : entity.getEnterpriseType().getId())
                .personType(personTypeSnapshot)
                .location(locationSnapshot)
                .build();

        List<SubjectSnapshot> subjects = subjectRepository.findAll().stream()
                .map((SubjectEntity s) -> SubjectSnapshot.builder().code(s.getCode()).name(s.getName()).build())
                .toList();

        return BackupManifest.builder()
                .version(1)
                .exportedAt(LocalDateTime.now())
                .sourceEnterpriseId(entity.getId())
                .enterprise(enterpriseSnapshot)
                .subjects(subjects)
                .build();
    }
}
