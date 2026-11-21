package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

public class WeightedAverageSalesEvent extends EventDto<KardexSalesDtoRequest, EventFactureType> {

    public WeightedAverageSalesEvent(KardexSalesDtoRequest data, EventFactureType type) {
        super(type, data);
    }
}
