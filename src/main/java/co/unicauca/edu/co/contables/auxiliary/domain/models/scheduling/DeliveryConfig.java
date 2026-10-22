package co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Configuración de entrega para un reporte programado.
 *
 * Define el canal ({@link EDeliveryWay}), el formato de salida
 * ({@link EAuxiliaryBookFormat}) y, si aplica, la configuración del
 * correo electrónico ({@link EmailConfig}) utilizada al enviar el reporte.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryConfig {
    private EDeliveryWay deliveryWay;
    private EAuxiliaryBookFormat format;
    private EmailConfig emailConfig;
}
