package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

public class PepsPurchaseEvent extends EventDto<KardexPurchaseDtoRequest, EventFactureType> {

    public PepsPurchaseEvent(KardexPurchaseDtoRequest data, EventFactureType type) {
        super(type, data);
    }
}
