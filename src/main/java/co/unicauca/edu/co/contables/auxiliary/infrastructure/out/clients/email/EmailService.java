package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.clients.email;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IEmailSenderPort;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @brief Adapter SMTP de envío de correos para reportes programados.
 *
 * Implementa {@link IEmailSenderPort} usando el {@link JavaMailSender}
 * autoconfigurado por Spring Boot a partir de las propiedades
 * {@code spring.mail.*}. Soporta cualquier proveedor SMTP estándar
 * (Gmail, Outlook, etc.). Por defecto usa Gmail con la misma cuenta
 * registrada en ms-notifications.
 *
 * <p>Propiedades consumidas via {@code @Value}:
 * <ul>
 *   <li>{@code notification-settings.mail.from}: remitente.</li>
 *   <li>{@code notification-settings.mail.default-subject}: asunto
 *       cuando el evento no especifica uno.</li>
 * </ul>
 */
@Service
@Slf4j
public class EmailService implements IEmailSenderPort {

    private static final String DEFAULT_BODY = "Adjunto encontrarás tu reporte programado.";
    private static final String DEFAULT_FILE_NAME = "scheduled_report.pdf";

    private final JavaMailSender mailSender;

    @Value("${notification-settings.mail.from:}")
    private String fromEmail;

    @Value("${notification-settings.mail.default-subject:Reporte programado}")
    private String defaultSubject;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    void reportConfiguration() {
        if (!StringUtils.hasText(fromEmail)) {
            log.warn("[EmailService] notification-settings.mail.from no configurado. "
                    + "Define MAIL_FROM o agrega 'notification-settings.mail.from' al YAML.");
        } else {
            log.info("[EmailService] SMTP ready. from={}", fromEmail);
        }
    }

    @Override
    public void sendReport(String to, String subject, String body, byte[] attachment, String fileName) {
        validateAttachment(attachment);
        List<String> recipients = resolveRecipients(to);

        if (!StringUtils.hasText(fromEmail)) {
            throw new IllegalStateException(
                    "notification-settings.mail.from is required to send emails.");
        }

        String resolvedSubject = StringUtils.hasText(subject) ? subject : defaultSubject;
        String resolvedBody = StringUtils.hasText(body) ? body : DEFAULT_BODY;
        String resolvedFileName = StringUtils.hasText(fileName) ? fileName : DEFAULT_FILE_NAME;
        String contentType = resolvedFileName.toLowerCase().endsWith(".pdf")
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        log.info("[EmailService] Sending via SMTP from={} to={} subject={} attachmentBytes={}",
                fromEmail, recipients, resolvedSubject, attachment.length);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(resolvedSubject);
            helper.setText(resolvedBody, false);
            helper.addAttachment(resolvedFileName, new ByteArrayResource(attachment), contentType);

            mailSender.send(mime);
            log.info("[EmailService] SMTP email delivered to {}", recipients);
        } catch (Exception ex) {
            log.error("[EmailService] SMTP delivery failed: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Email delivery failed: " + ex.getMessage(), ex);
        }
    }

    private List<String> resolveRecipients(String to) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Email recipient is required.");
        }
        List<String> recipients = Arrays.stream(to.split("[,;]"))
                .map(String::trim)
                .filter(r -> !r.isBlank())
                .toList();
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Email recipient is required.");
        }
        return recipients;
    }

    private void validateAttachment(byte[] attachment) {
        if (attachment == null || attachment.length == 0) {
            throw new IllegalArgumentException("Attachment content is empty.");
        }
    }
}
