package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums;

import lombok.Getter;

/**
 * @brief Enumeración que define los estados financieros principales
 *
 * Representa las dos categorías principales de estados financieros:
 * Estado de Situación Financiera (Balance General) y Estado de Resultados (Pérdidas y Ganancias).
 */
@Getter
public enum FinancialStatusEnum {
    STATEMENTFINANCIALPOSITION("Estado de Situacion Financiero"),
    INCOMESTATEMENT("Estado de Resultados");
    private final String state;

    FinancialStatusEnum(String state){
        this.state=state;
    }
}
