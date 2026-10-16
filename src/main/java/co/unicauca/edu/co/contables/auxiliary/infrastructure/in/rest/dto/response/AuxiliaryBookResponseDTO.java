package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @brief DTO de respuesta con el libro auxiliar y sus datos contables.
 *
 * Contiene el libro auxiliar generado junto con la lista de datos
 * contables asociados. Los campos nulos se omiten en la serialización
 * JSON para reducir el tamaño de la respuesta.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuxiliaryBookResponseDTO {
    private AuxiliaryBook auxiliaryBook;
    private List<?> accountingData;
}
