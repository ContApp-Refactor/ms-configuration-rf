package co.unicauca.edu.co.contables.accounting.treasury.application.output;

/**
 * Puerto de salida por el que el consumidor contable responde a tesoreria.
 *
 * <p>La respuesta siempre se publica en {@code accounting.result.exchange} y se
 * consume desde {@code accounting.treasury.result.queue} por
 * {@code TreasuryEventListener}. No publicar nada deja al comprobante o a la
 * baja colgada en {@code POSTING} / {@code VOIDING} para siempre.</p>
 */
public interface ITreasuryAccountingResultPublisher {

    /**
     * Respuesta a un evento de contabilizacion.
     *
     * @param sourceEventId identificador del evento que se responde; tesoreria lo
     *                      usa para deduplicar
     * @param operation     {@code CREATE} o {@code VOID}
     * @param documentType  {@code PAYMENT_VOUCHER} o {@code PAYABLE_WRITEOFF}
     * @param accepted      {@code true} si el asiento se genero o se anulo
     * @param accountingEntryId id del asiento generado, solo si se acepto
     * @param reason        motivo del rechazo, solo si se rechazo
     */
    record AccountingResultReply(
            String sourceEventId,
            String eventType,
            String tenantId,
            String enterpriseId,
            String correlationId,
            String operation,
            String documentType,
            Long documentId,
            boolean accepted,
            Long accountingEntryId,
            String reason) {}

    void publish(AccountingResultReply reply);
}
