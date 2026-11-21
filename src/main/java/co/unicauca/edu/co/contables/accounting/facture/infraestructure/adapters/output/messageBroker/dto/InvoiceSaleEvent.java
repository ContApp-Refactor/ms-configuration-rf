package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

public class InvoiceSaleEvent extends EventDto<ReceiptSalesDtoRequest, EventFactureType> {

    public static final EventFactureType TYPE = EventFactureType.SALE;

    public InvoiceSaleEvent(ReceiptSalesDtoRequest data) {
        super(TYPE, data);
    }
}
