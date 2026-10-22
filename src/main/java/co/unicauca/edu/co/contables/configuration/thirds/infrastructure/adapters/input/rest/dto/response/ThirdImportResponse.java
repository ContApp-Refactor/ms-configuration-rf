package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportErrorDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @brief DTO de respuesta para importación masiva de terceros desde Excel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdImportResponse {

    
    private String jobId;

    private String entId;

    private String fileName;

    private ImportStatus status;

    private Integer totalRecords;

    private Integer successfulImports;

    private Integer failedImports;

    private Integer duplicatesSkipped;

    private Integer progress;

    private List<ImportErrorDetail> errors;
}
