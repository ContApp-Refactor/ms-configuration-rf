package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums;

import lombok.Getter;

/**
 * @brief Enumeración que define la naturaleza de las cuentas contables
 *
 * Representa los dos tipos fundamentales de naturaleza en contabilidad:
 * Débito (incrementa cuentas de activo, gasto, costo) y Crédito (incrementa cuentas de pasivo, patrimonio, ingreso).
 */
@Getter
public enum NatureEnum {
    DEBIT("Debito"),
    CREDIT("Credito");

    private final String state;
    NatureEnum(String state){
        this.state=state;
    }
}
