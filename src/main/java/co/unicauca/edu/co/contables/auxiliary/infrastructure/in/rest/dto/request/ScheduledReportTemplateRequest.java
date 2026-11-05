package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Plantilla del reporte enviada por el frontend al crear/actualizar
 *        un reporte programado.
 *
 * Espejo de {@code AuxiliaryBookTemplate} pero con {@code pathLogotype}
 * como {@code String} para tolerar valores vacíos que JSON podría
 * mandar (la conversión a {@link java.net.URL} se hace en el mapper).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledReportTemplateRequest {
    private Long id;
    private String name;
    private String pathLogotype;
    private String alienation;
    private String font;
    private Integer fontSize;
    private String mainColor;
}
