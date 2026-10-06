package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO de solicitud para generar un libro auxiliar.
 *
 * Contiene los parámetros requeridos para la construcción de un libro
 * auxiliar: empresa, usuario, tipo de libro y criterios de filtrado.
 * Incluye validaciones Bean Validation para asegurar la integridad
 * de los datos antes de invocar el caso de uso correspondiente.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateAuxiliaryBookRequest {
    @NotEmpty(message = "Auxiliary Enterprise ID cannot be Empty")
    private String entId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Auxiliary Book Type cannot be Empty")
    private EAuxiliaryBookType type;

    @NotNull(message = "Auxiliary Book Criteria cannot be null")
    private AuxiliaryBookCriteria criteria;
}
