package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AccountingInfoProcessor;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;

import java.util.List;

/**
 * Contrato que deben implementar todas las estrategias de generación de libros auxiliares.
 *
 * <p>Cada implementación recibe los datos de contabilidad ya pre-filtrados por
 * {@code AuxiliaryBookCriteriaProcessor} (empresa, criterio de cuenta, centro de costo,
 * tercero, tipo de comprobante y fecha hasta {@code endDate}). La responsabilidad de
 * separar el periodo anterior del periodo actual recae en cada estrategia, ya que
 * el tratamiento del saldo inicial varía según el tipo de libro.</p>
 *
 * <p>Las implementaciones que requieren servicios externos (p.ej. {@code IThirdPartyInfoClient})
 * deben ser gestionadas como beans de Spring e inyectadas en {@code AuxiliaryBookProcessor}.</p>
 */
public interface IProcessStrategy {
    public List<?> process(AuxiliaryBookCriteria criteria, List<AccountingInfo> data, AccountingInfoProcessor accountingInfoProcessor);
}
