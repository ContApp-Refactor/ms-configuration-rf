package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.util.List;

/** Disponibilidad de devolucion de una factura; {@code state} es SIN_DEVOLUCION, PARCIAL o TOTAL (derivado). */
public record ReturnAvailabilityResponse(
        String invoiceId,
        String state,
        boolean returnable,
        List<ReturnAvailabilityLineResponse> lines) {
}
