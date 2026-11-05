package co.unicauca.edu.co.contables.auxiliary.domain.models.core.export;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

/**
 * @brief Modelo de dominio de la plantilla de exportación de un libro auxiliar.
 *
 * Define la apariencia del reporte: ruta del logotipo, alineación de
 * encabezados, fuente, tamaño y color principal. La plantilla es
 * suministrada por la entidad propietaria del libro.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuxiliaryBookTemplate {
    private Long id;
    private String name;
    private URL pathLogotype;
    private EAlignment alienation;
    private String font;
    private Integer fontSize;
    private String mainColor;
}
