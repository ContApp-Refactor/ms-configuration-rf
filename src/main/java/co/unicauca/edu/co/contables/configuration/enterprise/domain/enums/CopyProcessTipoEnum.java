package co.unicauca.edu.co.contables.configuration.enterprise.domain.enums;

/**
 * @brief Tipo de un proceso de copia, tal como lo consume el frontend
 * (Hito 6): {@code CopyProcess.tipo}.
 *
 * No reemplaza a {@link CopyOperationTypeEnum} (que sigue siendo el tipo de
 * operación interna backup/restore); este enum es el tipo de PROCESO
 * orientado al usuario, donde DUPLICATE sí existe como valor propio aunque
 * internamente se implemente como BACKUP+RESTORE encadenados.
 */
public enum CopyProcessTipoEnum {
    BACKUP,
    DUPLICATE,
    RESTORE
}
