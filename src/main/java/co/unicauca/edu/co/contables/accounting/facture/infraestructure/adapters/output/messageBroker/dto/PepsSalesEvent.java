package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

public class PepsSalesEvent extends EventDto<KardexSalesDtoRequest, EventFactureType> {

    public PepsSalesEvent(KardexSalesDtoRequest data, EventFactureType type) {
        super(type, data);
    }
}
