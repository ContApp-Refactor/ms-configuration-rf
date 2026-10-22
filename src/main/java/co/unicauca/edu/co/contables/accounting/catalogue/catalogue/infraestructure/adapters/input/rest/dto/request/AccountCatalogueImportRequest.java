package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief DTO para solicitud de importación masiva de cuentas desde Excel
 *
 * Contiene el archivo Excel con datos de cuentas y metadatos necesarios
 * para procesar la importación masiva en el catálogo contable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCatalogueImportRequest {

    @NotBlank(message = "El ID de empresa es requerido")
    private String entId;

    @NotNull(message = "El archivo Excel es requerido")
    private MultipartFile excelFile;

    private String fileName;

   
    public static AccountCatalogueImportRequest from(String entId, MultipartFile excelFile) {
        return AccountCatalogueImportRequest.builder()
                .entId(entId)
                .excelFile(excelFile)
                .fileName(excelFile != null ? excelFile.getOriginalFilename() : null)
                .build();
    }
}

