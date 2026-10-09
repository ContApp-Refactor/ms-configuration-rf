package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @brief Data Transfer Object (DTO) for Invoice Due Reminder Event.
 */

@Getter
@Setter
@RequiredArgsConstructor
public class InvoiceDueReminderEventDto {
    private Long thirdPartyId; 
    private List<InvoiceDetailEventDto> invoiceDetails;
}
