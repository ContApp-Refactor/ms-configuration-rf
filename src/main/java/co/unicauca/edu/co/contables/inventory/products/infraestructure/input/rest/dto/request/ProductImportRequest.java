package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief DTO de solicitud para importación masiva de productos
 *
 * Encapsula archivo Excel y metadatos requeridos para procesar
 * importación masiva de productos desde archivos estructurados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportRequest {

    @NotBlank(message = "El ID de empresa es requerido")
    private String entId;

    @NotNull(message = "El archivo Excel es requerido")
    private MultipartFile excelFile;

    private String fileName;

    /**
     * @brief Crea instancia desde parámetros del controlador
     *
     * Método factory para construir request de importación desde
     * parámetros HTTP comunes en controladores REST.
     *
     * @param entId ID de la empresa
     * @param excelFile Archivo Excel subido
     * @return Instancia configurada del request
     */
    public static ProductImportRequest from(String entId, MultipartFile excelFile) {
        return ProductImportRequest.builder()
                .entId(entId)
                .excelFile(excelFile)
                .fileName(excelFile != null ? excelFile.getOriginalFilename() : null)
                .build();
    }
}