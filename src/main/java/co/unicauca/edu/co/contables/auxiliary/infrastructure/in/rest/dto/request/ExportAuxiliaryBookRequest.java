package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @brief DTO de solicitud para exportar un libro auxiliar.
 *
 * Agrupa toda la información necesaria para producir un archivo
 * exportable (PDF, Excel, etc.): formato, nombre de la empresa, el libro
 * auxiliar ya generado, los datos contables y la plantilla del reporte.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportAuxiliaryBookRequest {
    @NotNull(message = "The format of the report cannot be Empty")
    private EAuxiliaryBookFormat format; // e.g., "PDF", "Excel"

    @NotEmpty(message = "Enterprise name cannot be Empty")
    private String entName;

    @NotNull(message = "The criteria used to create the auxiliary book cannot be null")
    private AuxiliaryBook auxiliaryBook;

    @NotNull(message = "The data from the auxiliary book cannot be null")
    private List<?> auxBookData;

    @NotNull(message = "The template information for the report cannot be null")
    private AuxiliaryBookTemplate infoReportTemplate;
}
