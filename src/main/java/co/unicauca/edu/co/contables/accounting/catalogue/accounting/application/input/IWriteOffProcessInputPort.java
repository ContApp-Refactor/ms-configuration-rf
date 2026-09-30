package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input;

import java.util.List;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.PortfolioWriteOff;

public interface IWriteOffProcessInputPort {
    /**
     * Procesa la confirmación de un castigo de cartera.
     * Este caso de uso es responsable de generar el asiento contable correspondiente
     * y actualizar los saldos de las cuentas afectadas.
     *
     * @param portfolioWriteOff El objeto de dominio que representa el castigo de cartera a confirmar.
     */
    void processWriteOffConfirmation(PortfolioWriteOff portfolioWriteOff);

    /**
     * Procesa la anulación (anulación) de un castigo de cartera.
     * Este caso de uso es responsable de encontrar el asiento contable original,
     * marcarlo como anulado y revertir el impacto en los saldos de las cuentas.
     *
     * @param portfolioWriteOff El objeto de dominio que representa el evento de anulación del castigo.
     */
    void processWriteOffAnnulment(PortfolioWriteOff portfolioWriteOff);

    /**
     * Marca una o varias facturas como castigadas.
     * @param invoiceIds Lista de IDs de las facturas a castigar.
     * @throws debt_payments.domain.exception.InvoiceNotFoundException si alguna de las facturas
     *         especificadas en la lista no se encuentra en el sistema.
     * @throws IllegalStateException si se intenta castigar una factura que ya ha sido pagada.
     */
    void writeOffInvoices(List<Long> invoiceIds);
}
