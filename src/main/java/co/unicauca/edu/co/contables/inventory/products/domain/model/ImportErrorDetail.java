package co.unicauca.edu.co.contables.inventory.products.domain.model;

import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportErrorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Detalle de error durante proceso de importación
 *
 * Contiene información completa sobre errores encontrados durante la importación
 * de productos, incluyendo ubicación, tipo y descripción del problema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorDetail {

    private Integer rowNumber;
    private Integer columnNumber;
    private String columnName;
    private String errorCode;
    private String errorMessage;
    private ImportErrorType errorType;
    private String fieldValue;
}