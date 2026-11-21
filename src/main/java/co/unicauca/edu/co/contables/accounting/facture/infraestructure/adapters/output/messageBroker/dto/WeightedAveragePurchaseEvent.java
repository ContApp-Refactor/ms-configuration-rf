package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

public class WeightedAveragePurchaseEvent extends EventDto<KardexPurchaseDtoRequest, EventFactureType> {

    public WeightedAveragePurchaseEvent(KardexPurchaseDtoRequest data, EventFactureType type) {
        super(type, data);
    }
}
