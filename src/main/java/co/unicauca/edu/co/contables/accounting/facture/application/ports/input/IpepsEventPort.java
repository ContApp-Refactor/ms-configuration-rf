package co.unicauca.edu.co.contables.accounting.facture.application.ports.input;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;

public interface IpepsEventPort {
    void publishPurchasePEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest);
    void publishSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnPurchasePEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishNonCommercialExitPEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishNonCommercialEntryPEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest);

}
