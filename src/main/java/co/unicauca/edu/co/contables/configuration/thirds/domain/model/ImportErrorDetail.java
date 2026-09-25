package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportErrorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO que representa un error específico durante la importación
 *
 * Proporciona información detallada sobre errores encontrados durante
 * procesos de importación masiva de terceros, facilitando la identificación
 * y corrección de problemas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorDetail {

   
    private Integer rowNumber;

   
    private Integer columnNumber;


    private String columnName;

   
    private String fieldValue;

   
    private String errorCode;

   
    private String errorMessage;

   
    private ImportErrorType errorType;
}
