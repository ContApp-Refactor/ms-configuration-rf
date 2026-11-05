package co.unicauca.edu.co.contables.notifications.infrastructure.output.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.notifications.application.output.IEmailProviderPort;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Adapter class for sending emails using JavaMailSender.
 * Implements the IEmailProviderPort interface.
 * Uses Spring's JavaMailSender to send invoice reminder emails.
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailAdapter implements IEmailProviderPort {

    private final JavaMailSender mailSender;

    /**
     * @brief Sender email address from application properties.
     */
    @Value("${notification-settings.mail.from}")
    private String fromEmail;

    /**
     * @brief Subject for invoice reminder emails from application properties.
     */
    @Value("${notification-settings.mail.invoice-reminder-subject:Recordatorio de Vencimiento de Facturas}")
    private String subject;

    /**
     * @brief Sends an invoice reminder email.
     * @param recipientName the name of the recipient
     * @param recipientEmail the email address of the recipient
     * @param htmlContent the HTML content of the email 
     */
    @Override
    public void sendInvoiceReminderEmail(String recipientName, String recipientEmail, String htmlContent) {
        log.info("Intentando enviar correo de recordatorio a: {}", recipientEmail);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true -> el contenido es HTML

            mailSender.send(mimeMessage);
            log.info("Correo enviado exitosamente a: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", recipientEmail, e.getMessage());
        }
    }
    
}
