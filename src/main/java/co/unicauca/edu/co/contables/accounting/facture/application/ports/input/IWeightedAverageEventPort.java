package co.unicauca.edu.co.contables.accounting.facture.application.ports.input;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;

public interface IWeightedAverageEventPort {
    void publishPurchaseWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest);
    void publishSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnPurchaseWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest);
    
    void publishNonCommercialExitWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishNonCommercialEntryWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest);
}
