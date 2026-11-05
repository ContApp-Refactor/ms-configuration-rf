package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.TreasuryEvent;

public interface ITreasuryEventPublisher {
    void enqueue(TreasuryEvent event);
}
