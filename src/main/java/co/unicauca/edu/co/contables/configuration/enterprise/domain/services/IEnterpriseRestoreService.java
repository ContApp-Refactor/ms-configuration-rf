package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * @brief Puerto de salida para el restore (import) de una empresa.
 *
 * Reconstruye, en una sola transacción local, la empresa, su tipo de
 * persona, su ubicación y las materias de un backup. Soporta
 * {@code inplace=true} (sobrescribe {@code targetEnterpriseId}: reemplaza su
 * tipo de persona y ubicación, da de baja los anteriores) o
 * {@code inplace=false} (crea una empresa nueva).
 */
public interface IEnterpriseRestoreService {

    UUID restoreFromBackupId(String backupId, boolean inplace, UUID targetEnterpriseId);

    UUID restoreFromFile(MultipartFile file, boolean inplace, UUID targetEnterpriseId);
}
