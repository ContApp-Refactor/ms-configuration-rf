package co.unicauca.edu.co.contables.configuration.enterprise.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Referencia de un backup de empresa ya creado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupReferenceDto {
    private String backupId;
    private UUID sourceEnterpriseId;
    private String fileName;
    private long sizeBytes;
    private LocalDateTime createdAt;
}
