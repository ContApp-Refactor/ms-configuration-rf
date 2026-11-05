package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Implementación de duplicar empresa: BACKUP del origen + RESTORE
 * ({@code inplace=false}) encadenados.
 *
 * El ZIP de backup generado es un artefacto intermedio (no uno que el
 * usuario pidió conservar), así que se elimina al terminar, haya tenido
 * éxito el restore o no.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDuplicateServiceImpl implements IEnterpriseDuplicateService {

    private final IEnterpriseBackupService backupService;
    private final IEnterpriseRestoreService restoreService;

    @Override
    public UUID duplicate(UUID enterpriseId) {
        BackupReferenceDto reference = backupService.backup(enterpriseId);
        try {
            return restoreService.restoreFromBackupId(reference.getBackupId(), false, null);
        } finally {
            try {
                backupService.deleteBackup(reference.getBackupId());
            } catch (RuntimeException e) {
                log.warn("No se pudo limpiar el backup temporal '{}' usado por duplicate", reference.getBackupId(),
                        e);
            }
        }
    }
}
