package co.unicauca.edu.co.contables.billing.domain.service;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnLineAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de dominio sin estado (D1) que calcula cuanto de una factura puede devolverse
 * (RET-3, RN-19): disponible = vendido - ya devuelto, contando SOLO devoluciones EMITIDA (ni
 * borradores ni anuladas). Tambien deriva el estado de devolucion (RET-9, RN-21) y los ordinales
 * por numero (RET-10, RN-20). Java puro.
 */
public final class ReturnAvailabilityCalculator {

    private static final int QUANTITY_SCALE = 3;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(QUANTITY_SCALE);

    /**
     * Disponibilidad por linea de la factura. {@code returns} puede traer cualquier devolucion;
     * solo cuentan las EMITIDA del tipo de devolucion de esta factura que la referencian.
     */
    public ReturnAvailability availability(BillingDocument invoice, Collection<BillingDocument> returns) {
        Objects.requireNonNull(invoice, "invoice");
        if (!invoice.type().isInvoice()) {
            throw new IllegalArgumentException(invoice.type() + " no es una factura");
        }
        Map<Integer, BigDecimal> returnedBySourceLine = returnedBySourceLine(invoice, returns);

        List<ReturnLineAvailability> lines = new ArrayList<>(invoice.lines().size());
        boolean anyReturned = false;
        boolean anyAvailable = false;
        for (DocumentLine line : invoice.lines()) {
            BigDecimal sold = line.quantity().value();
            BigDecimal returned = returnedBySourceLine.getOrDefault(line.lineNo(), ZERO);
            BigDecimal available = sold.subtract(returned).max(ZERO).setScale(QUANTITY_SCALE);
            anyReturned |= returned.signum() > 0;
            anyAvailable |= available.signum() > 0;
            lines.add(new ReturnLineAvailability(line.lineNo(), line.productId(), line.productName(), sold,
                    returned, available, line.unitPrice(), line.taxRate()));
        }
        boolean returnable = invoice.status() == DocumentStatus.EMITIDA && anyAvailable;
        return new ReturnAvailability(invoice.id(), lines, status(anyReturned, anyAvailable), returnable);
    }

    /**
     * Valida las lineas pedidas contra la disponibilidad (RET-3). {@code otherReturns} son las
     * demas devoluciones de la factura (excluida la que se esta guardando o emitiendo). Una linea
     * sin referencia o repetida es INVALID_LINE; una referencia inexistente en la factura es
     * RETURN_LINE_NOT_FOUND; una cantidad mayor que la disponible es RETURN_QUANTITY_EXCEEDED.
     */
    public void assertWithinAvailability(BillingDocument invoice, Collection<BillingDocument> otherReturns,
            List<DocumentLine> requestedLines) {
        ReturnAvailability availability = availability(invoice, otherReturns);
        if (requestedLines == null || requestedLines.isEmpty()) {
            return;
        }
        Map<Integer, ReturnLineAvailability> byLine = new HashMap<>();
        for (ReturnLineAvailability line : availability.lines()) {
            byLine.put(line.lineNo(), line);
        }
        Set<Integer> seen = new HashSet<>();
        for (DocumentLine requested : requestedLines) {
            Integer sourceLineNo = requested.sourceLineNo();
            if (sourceLineNo == null) {
                throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                        "Toda linea de devolucion debe referenciar una linea de la factura original");
            }
            if (!seen.add(sourceLineNo)) {
                throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                        "La linea " + sourceLineNo + " de la factura original esta repetida");
            }
            ReturnLineAvailability target = byLine.get(sourceLineNo);
            if (target == null) {
                throw new BillingException(BillingErrorCode.BILLING_RETURN_LINE_NOT_FOUND,
                        "La factura no tiene la linea " + sourceLineNo);
            }
            if (requested.quantity().value().compareTo(target.available()) > 0) {
                throw new BillingException(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED,
                        "La linea " + sourceLineNo + " admite como maximo " + target.available().toPlainString()
                                + " unidades a devolver");
            }
        }
    }

    /**
     * Ordinal (1..n) de cada devolucion dentro de su factura, por consecutivo ascendente. El mapa
     * de entrada es {@code id de devolucion -> valor de su numero}; solo deben venir devoluciones
     * numeradas (EMITIDA y ANULADA). El resultado conserva el orden ascendente.
     */
    public static Map<UUID, Integer> ordinals(Map<UUID, Long> numberValueByReturnId) {
        Objects.requireNonNull(numberValueByReturnId, "numberValueByReturnId");
        List<Map.Entry<UUID, Long>> sorted = new ArrayList<>(numberValueByReturnId.entrySet());
        sorted.sort(Map.Entry.<UUID, Long>comparingByValue().thenComparing(Map.Entry.<UUID, Long>comparingByKey()));
        Map<UUID, Integer> result = new LinkedHashMap<>();
        int ordinal = 1;
        for (Map.Entry<UUID, Long> entry : sorted) {
            result.put(entry.getKey(), ordinal++);
        }
        return result;
    }

    /**
     * Ordinales a partir de documentos: toma las devoluciones numeradas (EMITIDA o ANULADA, que
     * conservan su ordinal) e ignora los borradores, que no tienen numero (RN-20).
     */
    public static Map<UUID, Integer> ordinalsOf(Collection<BillingDocument> returns) {
        Objects.requireNonNull(returns, "returns");
        Map<UUID, Long> numbers = new HashMap<>();
        for (BillingDocument doc : returns) {
            if (doc.type().isReturn() && doc.number() != null && doc.status() != DocumentStatus.BORRADOR) {
                numbers.put(doc.id(), doc.number().value());
            }
        }
        return ordinals(numbers);
    }

    private static Map<Integer, BigDecimal> returnedBySourceLine(BillingDocument invoice,
            Collection<BillingDocument> returns) {
        Map<Integer, BigDecimal> returned = new HashMap<>();
        if (returns == null) {
            return returned;
        }
        for (BillingDocument doc : returns) {
            if (!countsAgainst(invoice, doc)) {
                continue;
            }
            for (DocumentLine line : doc.lines()) {
                if (line.sourceLineNo() != null) {
                    returned.merge(line.sourceLineNo(), line.quantity().value(), BigDecimal::add);
                }
            }
        }
        return returned;
    }

    private static boolean countsAgainst(BillingDocument invoice, BillingDocument candidate) {
        return candidate.status() == DocumentStatus.EMITIDA
                && candidate.type() == invoice.type().returnType()
                && candidate.original() != null
                && invoice.id().equals(candidate.original().id());
    }

    private static ReturnStatus status(boolean anyReturned, boolean anyAvailable) {
        if (!anyReturned) {
            return ReturnStatus.SIN_DEVOLUCION;
        }
        return anyAvailable ? ReturnStatus.PARCIAL : ReturnStatus.TOTAL;
    }
}
