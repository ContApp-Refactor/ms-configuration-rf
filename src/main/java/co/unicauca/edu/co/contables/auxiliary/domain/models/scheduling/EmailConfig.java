package co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Configuración de correo electrónico para la entrega del reporte.
 *
 * Contiene el destinatario y las plantillas de asunto y cuerpo utilizadas
 * al despachar el reporte por correo cuando el canal seleccionado así
 * lo indica.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailConfig {
    private String to;
    private String subjectTemplate;
    private String bodyTemplate;
}
