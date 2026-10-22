package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de respuesta con la referencia de un backup recién creado.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackupReferenceRes {
    private String backupId;
    private UUID sourceEnterpriseId;
    private String fileName;
    private long sizeBytes;
    private LocalDateTime createdAt;
}
