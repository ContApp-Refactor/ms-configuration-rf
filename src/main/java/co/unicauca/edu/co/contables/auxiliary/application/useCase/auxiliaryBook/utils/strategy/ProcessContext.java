package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AccountingInfoProcessor;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Contexto del patrón Strategy para el procesamiento de libros auxiliares.
 *
 * Mantiene la estrategia activa ({@link IProcessStrategy}) y expone un
 * método para delegar en ella el procesamiento de los datos contables
 * según el tipo de libro auxiliar seleccionado.
 */
@Service
@NoArgsConstructor
@Setter
public class ProcessContext {

    private IProcessStrategy strategy;

    public List<?> executeStrategyProcess(AuxiliaryBookCriteria criteria, List<AccountingInfo> data, AccountingInfoProcessor accountingInfoProcessor){
        return strategy.process(criteria, data, accountingInfoProcessor);
    }
}
