package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Regla: el N.º de factura del proveedor no se repite DENTRO DE UN MISMO PROVEEDOR (clave:
 * tenant + empresa + proveedor + numero). Aplica solo a PURCHASE_INVOICE; las devoluciones de
 * compra copian el numero de la original y nunca se validan.
 *
 * <p>Decisiones: la comparacion ignora mayusculas y espacios en los bordes; cuentan los documentos
 * en BORRADOR y EMITIDA, y las ANULADA NO cuentan (permiten reutilizar el numero). Se valida al
 * crear/actualizar el borrador y al emitir, siempre excluyendo al propio documento. Los duplicados
 * que ya existieran no se tocan: la regla solo corre en esas operaciones.
 *
 * <p>Limitacion: es una comprobacion previa dentro de la transaccion, no una restriccion de base
 * de datos (un indice unico parcial no es viable sin Flyway con {@code ddl-auto=update}); dos
 * creaciones de borradores simultaneas del mismo proveedor y numero podrian pasar ambas. La
 * emision se serializa por el bloqueo de fila del propio documento, no entre documentos distintos.
 */
final class SupplierInvoiceNumberGuard {

    private final BillingDocumentRepositoryPort repository;

    SupplierInvoiceNumberGuard(BillingDocumentRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /**
     * @param excludeId el propio documento (nulo al crear)
     * @param counterpartyId proveedor del documento; nulo si aun no se eligio (nada que comprobar)
     * @param supplierInvoiceNumber numero tal como llega o se guardo; vacio = nada que comprobar
     */
    void assertUnique(String enterpriseId, DocumentType type, Long counterpartyId, String supplierInvoiceNumber,
            UUID excludeId) {
        if (type != DocumentType.PURCHASE_INVOICE || counterpartyId == null || supplierInvoiceNumber == null
                || supplierInvoiceNumber.isBlank()) {
            return;
        }
        String number = supplierInvoiceNumber.trim();
        Optional<BillingDocumentSummary> existing = repository.findPurchaseInvoiceBySupplierNumber(
                enterpriseId, counterpartyId, number, excludeId);
        if (existing.isPresent()) {
            throw new BillingException(BillingErrorCode.BILLING_DUPLICATE_SUPPLIER_INVOICE, message(number,
                    existing.get()));
        }
    }

    private static String message(String number, BillingDocumentSummary existing) {
        String base = "Ya existe una factura de compra de este proveedor con el N.º " + number;
        if (existing.status() == DocumentStatus.EMITIDA && existing.number() != null) {
            base += " (" + existing.number().display() + ")";
        }
        return base + ". Usa un número diferente.";
    }
}
