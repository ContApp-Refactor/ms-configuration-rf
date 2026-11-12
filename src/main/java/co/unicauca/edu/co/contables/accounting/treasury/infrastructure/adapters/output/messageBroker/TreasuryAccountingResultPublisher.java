package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker;

import java.time.Instant;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.config.TreasuryRabbitConfig;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryRabbitDtos;
import co.unicauca.edu.co.contables.commons.config.aspect.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publica la respuesta del consumidor contable en {@code accounting.result.exchange}.
 *
 * <p>El {@code RabbitTemplate} de la aplicacion lleva {@code Jackson2JsonMessageConverter},
 * asi que basta con enviar el envelope como objeto: sale serializado en JSON y
 * {@code TreasuryEventListener} lo deserializa de vuelta en
 * {@code AccountingResultEnvelope}.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TreasuryAccountingResultPublisher implements ITreasuryAccountingResultPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final JwtTokenService jwtTokenService;

    @Override
    public void publish(AccountingResultReply reply) {
        String eventId = UUID.randomUUID().toString();
        TreasuryRabbitDtos.AccountingResult payload = new TreasuryRabbitDtos.AccountingResult(
                eventId,
                reply.sourceEventId(),
                reply.operation(),
                reply.documentType(),
                reply.documentId(),
                reply.accepted(),
                reply.accountingEntryId(),
                reply.reason(),
                reply.tenantId(),
                reply.enterpriseId());
        TreasuryRabbitDtos.AccountingResultEnvelope envelope =
                new TreasuryRabbitDtos.AccountingResultEnvelope(
                        eventId,
                        reply.documentType() + "." + reply.operation() + ".RESULT",
                        1,
                        Instant.now(),
                        reply.tenantId(),
                        reply.enterpriseId(),
                        reply.correlationId(),
                        new TreasuryRabbitDtos.SourceDocument(reply.documentType(), reply.documentId()),
                        payload);
        try {
            rabbitTemplate.convertAndSend(TreasuryRabbitConfig.RESULT_EXCHANGE, "", envelope,
                    message -> {
                        message.getMessageProperties().setMessageId(payload.eventId());
                        message.getMessageProperties().setHeader("eventId", payload.eventId());
                        message.getMessageProperties().setHeader("eventType", envelope.eventType());
                        message.getMessageProperties().setHeader("tenantId", reply.tenantId());
                        message.getMessageProperties().setHeader("x-tenant-id", reply.tenantId());
                        String token = resolveJwtToken();
                        if (token != null && !token.isBlank()) {
                            message.getMessageProperties().setHeader("x-jwt-token", token);
                        }
                        return message;
                    });
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible publicar el resultado contable "
                    + reply.operation() + " de " + reply.documentType() + " "
                    + reply.documentId(), ex);
        }
        log.info("Resultado contable {} {} {} publicado (aceptado={})",
                reply.operation(), reply.documentType(), reply.documentId(), reply.accepted());
    }

    /**
     * JWTContextRabbitMqAspect exige el header {@code x-jwt-token} para montar el
     * TenantContext del listener receptor; sin el, findById filtra contra
     * {@code tenant_id = null} y el comprobante "no existe". JwtTokenService devuelve
     * el token del contexto Rabbit cuando corremos dentro de un listener.
     */
    private String resolveJwtToken() {
        try {
            return jwtTokenService.getToken();
        } catch (IllegalStateException ex) {
            log.debug("No hay JWT en el contexto actual; el resultado se publica sin x-jwt-token");
            return null;
        }
    }
}
