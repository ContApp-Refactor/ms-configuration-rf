package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.recovery;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSyncDto;
import org.springframework.stereotype.Component;

@Component
public class InvoiceRecoveryAction implements IEventRecoveryActionPort<EventDto<InvoiceSyncDto>> {

    @Override
    public boolean executeRecoveryAction(EventDto<InvoiceSyncDto> event) {
        // TODO por hacer para Rodrigo
        throw new UnsupportedOperationException("Unimplemented method 'executeRecoveryAction'");
    }

    @Override
    public boolean canHandle(EventDto<InvoiceSyncDto> event) {
        // TODO por hacer para Rodrigo
        throw new UnsupportedOperationException("Unimplemented method 'canHandle'");
    }
    
}
