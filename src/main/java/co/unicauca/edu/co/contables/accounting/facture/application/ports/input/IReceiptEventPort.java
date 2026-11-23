package co.unicauca.edu.co.contables.accounting.facture.application.ports.input;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;

public interface IReceiptEventPort {
        void publishSaleReceiptEvent(ReceiptSalesDtoRequest receiptSalesDtoRequest);
        void publishPurchaseInvoiceEvent(PurchaseInvoiceEventDto event);
}
