package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;

/**
 * Puerto de entrada del consumidor contable de tesoreria.
 *
 * <p>Cada operacion traduce un evento de dominio de tesoreria en un asiento
 * contable (o en su anulacion) y publica siempre una respuesta en
 * {@code accounting.result.exchange}, aceptada o rechazada. Tesoreria no puede
 * quedar esperando: si el consumidor no puede contabilizar, tiene que responder
 * {@code accepted=false} con un motivo, no callarse.</p>
 */
public interface ITreasuryAccountingInputPort {

    /** Datos del evento que no dependen del documento concreto que se contabiliza. */
    record AccountingEventContext(
            String eventId,
            String eventType,
            String tenantId,
            String enterpriseId,
            String correlationId) {}

    /** Contabiliza un comprobante de pago recien confirmado. */
    void postPaymentVoucher(PaymentVoucher voucher, AccountingEventContext context);

    /** Anula el asiento de un comprobante de pago. */
    void voidPaymentVoucher(Long voucherId, AccountingEventContext context);

    /** Contabiliza una baja de obligaciones recien confirmada. */
    void postPayableWriteOff(PayableWriteOff writeOff, AccountingEventContext context);

    /** Anula el asiento de una baja de obligaciones. */
    void voidPayableWriteOff(Long writeOffId, AccountingEventContext context);
}
