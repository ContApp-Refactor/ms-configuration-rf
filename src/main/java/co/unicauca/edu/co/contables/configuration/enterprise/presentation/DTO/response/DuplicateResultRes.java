package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de respuesta del resultado de duplicar una empresa.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DuplicateResultRes {
    private UUID sourceEnterpriseId;
    private UUID newEnterpriseId;
}
