package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils;

import java.util.List;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IThirdPartyInfoClient;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy.*;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IAccountingInfoClient;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;

import lombok.RequiredArgsConstructor;

/**
 * Orquesta la generación de datos para cada tipo de libro auxiliar.
 *
 * <p>Delega el pre-filtrado de los datos contables (empresa, rango de cuentas,
 * centro de costo, tercero, tipo de comprobante y fecha hasta {@code endDate}) a
 * {@link AuxiliaryBookCriteriaProcessor}.  Luego selecciona la estrategia
 * correcta según {@link co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType}
 * y le entrega los datos ya filtrados.</p>
 *
 * <p><b>Contrato del filtrado previo:</b> {@code AuxiliaryBookCriteriaProcessor}
 * filtra hasta {@code endDate} (inclusive) y aplica todos los criterios de negocio
 * configurados, pero NO aplica filtro de {@code startDate}.  Esto es intencional:
 * las estrategias que necesitan calcular el saldo inicial (Mayor, Cuenta, Tercero,
 * Inventario, Movimientos) reciben también los datos del periodo anterior para
 * poder acumular el saldo histórico.  Cada estrategia es responsable de separar
 * los periodos internamente.</p>
 *
 * <p><b>Inyección de dependencias:</b> Las estrategias que requieren servicios
 * externos se construyen aquí, donde Spring ya ha resuelto todas las dependencias.
 * Esto evita el antipatrón de instanciar estrategias con {@code new} dentro de
 * un switch, que impedía la inyección de clientes como {@link IThirdPartyInfoClient}.</p>
 */
@Service
@RequiredArgsConstructor
public class AuxiliaryBookProcessor {

    private final AccountingInfoProcessor accountingInfoProcessor;
    private final AuxiliaryBookCriteriaProcessor auxiliaryBookCriteriaProcessor;
    private final IThirdPartyInfoClient thirdPartyInfoClient;

    public List<?> processAuxiliaryBookData(IAccountingInfoClient accountingInfoQueryPort, AuxiliaryBook book) {
        AuxiliaryBookCriteria criteria = book.getCriteria();
        List<AccountingInfo> filteredAccountData =
                auxiliaryBookCriteriaProcessor.processAccountingInfo(accountingInfoQueryPort, book);
        IProcessStrategy strategy = resolveStrategy(book);
        return strategy.process(criteria, filteredAccountData, accountingInfoProcessor);
    }

    /**
     * Selecciona e instancia la estrategia correspondiente al tipo de libro.
     *
     * <p>Las estrategias sin dependencias externas se instancian directamente.
     * {@link ThirdPartyStrategy} recibe {@code thirdPartyInfoClient} por
     * constructor, ya que necesita consultar el microservicio de terceros.</p>
     */
    private IProcessStrategy resolveStrategy(AuxiliaryBook book) {
        return switch (book.getType()) {
            case INVENTORY_AND_BALANCES -> new InventoryAndBalancesStrategy();
            case DIARY                  -> new DiaryStrategy();
            case MAJOR_AND_BALANCES     -> new MajorAndBalancesStrategy();
            case ACCOUNT                -> new AccountStrategy();
            case THIRD_PARTY            -> new ThirdPartyStrategy(thirdPartyInfoClient);
            case ACCOUNTING_MOVEMENT    -> new AccountingMovementStrategy();
        };
    }
}

