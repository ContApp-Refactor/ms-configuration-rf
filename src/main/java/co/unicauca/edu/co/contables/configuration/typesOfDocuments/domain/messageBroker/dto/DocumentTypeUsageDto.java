package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para eventos de uso de tipos de documento
 *
 * Contiene la información necesaria para actualizar el contador de uso
 * cuando otros servicios notifican que han utilizado un tipo de documento.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentTypeUsageDto {
    private Long documentTypeId;
    private String enterpriseId;
    private Integer quantityUsed;
}
