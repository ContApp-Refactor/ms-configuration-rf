package co.unicauca.edu.co.contables.auxiliary.application.ports.out;

/**
 * @brief Puerto de salida para el envío de reportes por correo electrónico.
 *
 * Define el contrato que usa la capa de aplicación para entregar los
 * reportes generados como adjunto a un destinatario, desacoplando el
 * caso de uso de la implementación concreta del proveedor de correo.
 */
public interface IEmailSenderPort {
    /**
     * @brief Envía un reporte como adjunto a un destinatario.
     * @param to Dirección de correo del destinatario.
     * @param subject Asunto del correo.
     * @param body Cuerpo del correo en texto plano o HTML.
     * @param attachment Contenido binario del reporte a adjuntar.
     * @param fileName Nombre del archivo adjunto.
     */
    void sendReport(String to, String subject, String body, byte[] attachment, String fileName);
}
