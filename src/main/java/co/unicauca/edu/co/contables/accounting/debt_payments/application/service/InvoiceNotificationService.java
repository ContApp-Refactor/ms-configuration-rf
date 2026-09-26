package co.unicauca.edu.co.contables.accounting.debt_payments.application.service;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IInvoiceNotificationUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceNotificationEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * @brief Service class for handling invoice notifications.
 * This class processes due invoices and publishes consolidated
 * notification events for clients with pending invoices.
 */

@Service
@RequiredArgsConstructor
public class InvoiceNotificationService implements IInvoiceNotificationUseCase {

    private final IInvoiceProviderPort invoiceProviderPort;
    private final IInvoiceNotificationEventPublisher notificationPublisher;
    private static final int DAYS_BEFORE_DUE = 5; 

    /**
     * @brief Process and publish due invoice notifications.
     * This method retrieves invoices that are due in a specified number of days,
     * groups them by third party ID, constructs consolidated notification events, 
     * and publishes them.
     */
    @Override
    public void processAndPublishDueInvoices() {
        LocalDate targetDate = LocalDate.now().plusDays(DAYS_BEFORE_DUE);
        List<InvoiceReplica> dueInvoices = invoiceProviderPort.findInvoicesByExpirationDate(targetDate);

        // 2. AGRUPAR las facturas por el ID del cliente (thirdPartyId)
        Map<Long, List<InvoiceReplica>> invoicesByClient = dueInvoices.stream()
                .collect(Collectors.groupingBy(InvoiceReplica::getThirdId));

        // 3. ITERAR sobre el mapa de clientes agrupados
        for (Map.Entry<Long, List<InvoiceReplica>> entry : invoicesByClient.entrySet()) {
            Long thirdPartyId = entry.getKey();
            List<InvoiceReplica> clientInvoices = entry.getValue();

            // 4. Construir el evento CONSOLIDADO para este cliente
            InvoiceDueReminderEventDto event = buildConsolidatedEvent(thirdPartyId, clientInvoices);
            
            // 5. Publicar UN ÚNICO evento por cliente
            notificationPublisher.publishInvoiceDueReminder(event);
        }
    }

    private InvoiceDueReminderEventDto buildConsolidatedEvent(Long thirdPartyId, List<InvoiceReplica> invoices) {
        // Mapear la lista de InvoiceReplica a una lista de InvoiceDetailDto
        List<InvoiceDetailEventDto> invoiceDetails = invoices.stream()
                .map(this::buildInvoiceDetail)
                .collect(Collectors.toList());

        // Crear el DTO consolidado
        InvoiceDueReminderEventDto consolidatedDto = new InvoiceDueReminderEventDto();
        consolidatedDto.setThirdPartyId(thirdPartyId);
        consolidatedDto.setInvoiceDetails(invoiceDetails);
        
        return consolidatedDto;
    }

    /**
     * @brief Build InvoiceDetailEventDto from InvoiceReplica.
     * @param invoice The InvoiceReplica instance.
     * @return The constructed InvoiceDetailEventDto.
     */
    private InvoiceDetailEventDto buildInvoiceDetail(InvoiceReplica invoice) {
        InvoiceDetailEventDto detailDto = new InvoiceDetailEventDto();
        detailDto.setInvoiceCode(Long.parseLong(invoice.getFactCode()));
        detailDto.setInvoiceId(invoice.getId());
        detailDto.setExpirationDate(invoice.getExpirationDate());
        detailDto.setTotalAmount(invoice.getTotalValue());
        detailDto.setPendingValue(invoice.getPendingValue());
        return detailDto;
    }
    
}
