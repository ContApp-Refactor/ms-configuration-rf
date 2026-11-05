package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.recovery;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.EventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.InvoiceSyncDto;



@Component("invoiceAccountingRecoveryAction")
public class InvoiceRecoveryAction implements IEventRecoveryActionPort<EventDTO<InvoiceSyncDto>> {

    @Override
    public boolean executeRecoveryAction(EventDTO<InvoiceSyncDto> event) {
        // TODO por hacer para Rodrigo
        throw new UnsupportedOperationException("Unimplemented method 'executeRecoveryAction'");
    }

    @Override
    public boolean canHandle(EventDTO<InvoiceSyncDto> event) {
        // TODO por hacer para Rodrigo
        throw new UnsupportedOperationException("Unimplemented method 'canHandle'");
    }
    
}
