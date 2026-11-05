package co.unicauca.edu.co.contables.auxiliary.domain.models.core.export;

import java.util.List;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio con la información para exportar un libro auxiliar.
 *
 * Reúne el formato solicitado, el nombre de la entidad, el libro auxiliar,
 * los datos ya procesados y la plantilla visual a aplicar. Es el contrato
 * de entrada para el generador de reportes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportInfo {
    private EAuxiliaryBookFormat format;
    private String entName;
    private AuxiliaryBook auxiliaryBook;
    private List<?> auxBookData;
    private AuxiliaryBookTemplate infoReportTemplate;
}
