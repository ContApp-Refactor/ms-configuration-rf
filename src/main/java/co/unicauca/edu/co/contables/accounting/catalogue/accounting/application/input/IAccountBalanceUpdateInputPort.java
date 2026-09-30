package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;

public interface IAccountBalanceUpdateInputPort {
    /**
     * Actualiza los saldos de todas las cuentas contables involucradas en un asiento contable.
     * Este método se encarga de la lógica de naturaleza (débito/crédito) y la actualización jerárquica.
     * @param accountingEntry El asiento contable que contiene los movimientos a procesar.
     */
    void updateBalancesFromAccountingEntry(AccountingEntry accountingEntry);

     /**
     * Revierte los saldos de las cuentas contables basándose en los movimientos de un asiento original.
     * Esta operación es la inversa de updateBalancesFromAccountingEntry.
     * @param accountingEntry El asiento contable original que se va a anular.
     */
    void reverseBalancesFromAccountingEntry(AccountingEntry accountingEntry); // NUEVO MÉTODO
}
