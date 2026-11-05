package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief DTO para solicitudes de importación masiva de terceros desde Excel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdImportRequest {

    @NotBlank(message = "El identificador de entidad es obligatorio")
    @Size(max = 50, message = "El ID de la empresa no puede exceder los 50 caracteres")
    private String entId;

    @NotNull(message = "El archivo Excel es obligatorio")
    private MultipartFile excelFile;

    private String fileName;
}
