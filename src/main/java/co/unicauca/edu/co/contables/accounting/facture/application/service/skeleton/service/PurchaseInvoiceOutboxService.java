package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceOutboxEvent;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.PurchaseInvoiceOutboxRepository;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.PurchaseInvoiceEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceOutboxService {
    private final PurchaseInvoiceOutboxRepository repository;
    private final ObjectMapper mapper;
    private final PayableAccountCodeResolver payableAccountCodeResolver;
    private final PurchaseInvoiceEventPublisher publisher;

    public void enqueue(SkeletonFacture facture, String eventType) {
        try {
            String eventId = UUID.randomUUID().toString();
            String tenant = TenantContext.getTenantId();
            String payableCode = payableAccountCodeResolver.resolveOrFail(
                    facture.getAccountingAccount(), facture.getEntId());
            PurchaseInvoiceEventDto body = PurchaseInvoiceEventDto.builder()
                    .invoiceId(facture.getId())
                    .reference(String.valueOf(facture.getFactCode()))
                    .enterpriseId(facture.getEntId())
                    .supplierId(facture.getThId())
                    .originalAmount(new BigDecimal(facture.getTotalValue()))
                    .paidAmount(new BigDecimal(facture.getTotalPay()))
                    .pendingAmount(new BigDecimal(facture.getPendingValue()))
                    .issueDate(resolveIssueDate(facture))
                    .dueDate(facture.getExpirationDate())
                    .payableAccountId(facture.getAccountingAccount())
                    .payableAccountCode(payableCode)
                    .active(facture.getPurchaseStatus() != PurchaseInvoiceStatus.VOIDED)
                    .tenantId(tenant)
                    .build();
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", eventId);
            envelope.put("eventType", eventType);
            envelope.put("eventVersion", 1);
            envelope.put("occurredAt", Instant.now());
            envelope.put("tenantId", tenant);
            envelope.put("enterpriseId", facture.getEntId());
            envelope.put("correlationId", eventId);
            envelope.put("sourceDocument", Map.of("type", "PURCHASE_INVOICE", "id", facture.getId()));
            envelope.put("payload", body);
            PurchaseInvoiceOutboxEvent event = new PurchaseInvoiceOutboxEvent();
            event.setEventId(eventId);
            event.setEventType(eventType);
            event.setPayload(mapper.writeValueAsString(envelope));
            event.setTenantId(tenant);
            repository.save(event);
            publisher.publish(event);
            event.setStatus(PurchaseInvoiceOutboxEvent.Status.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setLastError(null);
            repository.save(event);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible publicar el evento de compra", ex);
        }
    }

    private LocalDate resolveIssueDate(SkeletonFacture facture) {
        if (facture.getIssueDate() != null) {
            return facture.getIssueDate();
        }
        return facture.getCreatedAt() != null ? facture.getCreatedAt().toLocalDate() : LocalDate.now();
    }
}
