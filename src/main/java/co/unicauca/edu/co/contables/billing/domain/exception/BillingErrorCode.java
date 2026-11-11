package co.unicauca.edu.co.contables.billing.domain.exception;

/**
 * Codigos de error del modulo billing. Cada codigo declara su categoria; la capa REST
 * traduce categoria a HTTP, de modo que el dominio no conoce HTTP ni Spring.
 */
public enum BillingErrorCode {

    // NOT_FOUND
    BILLING_DOCUMENT_NOT_FOUND(ErrorCategory.NOT_FOUND),

    // CONFLICT
    BILLING_INVALID_STATE(ErrorCategory.CONFLICT),
    BILLING_CONCURRENT_MODIFICATION(ErrorCategory.CONFLICT),
    BILLING_HAS_EMITTED_RETURNS(ErrorCategory.CONFLICT),
    BILLING_ORIGINAL_NOT_EMITTED(ErrorCategory.CONFLICT),
    BILLING_IDEMPOTENCY_CONFLICT(ErrorCategory.CONFLICT),
    BILLING_IDEMPOTENCY_IN_PROGRESS(ErrorCategory.CONFLICT),
    BILLING_DUPLICATE_SUPPLIER_INVOICE(ErrorCategory.CONFLICT),

    // UNPROCESSABLE
    BILLING_DRAFT_INCOMPLETE(ErrorCategory.UNPROCESSABLE),
    BILLING_COUNTERPARTY_NOT_FOUND(ErrorCategory.UNPROCESSABLE),
    BILLING_PRODUCT_NOT_FOUND(ErrorCategory.UNPROCESSABLE),
    BILLING_ENTERPRISE_NOT_FOUND(ErrorCategory.UNPROCESSABLE),
    BILLING_INACTIVE_REFERENCE(ErrorCategory.UNPROCESSABLE),
    // Metodo de pago de la factura de venta (se exige al emitir; cuenta bancaria solo si el metodo es de banco)
    BILLING_PAYMENT_METHOD_REQUIRED(ErrorCategory.UNPROCESSABLE),
    BILLING_PAYMENT_METHOD_INVALID(ErrorCategory.UNPROCESSABLE),
    // Retenciones de la factura (codigo no permitido o repetido, impuesto invalido, total negativo)
    BILLING_WITHHOLDING_INVALID(ErrorCategory.UNPROCESSABLE),
    BILLING_WITHHOLDING_TAX_INVALID(ErrorCategory.UNPROCESSABLE),
    BILLING_TOTAL_NEGATIVE(ErrorCategory.UNPROCESSABLE),
    BILLING_RETURN_QUANTITY_EXCEEDED(ErrorCategory.UNPROCESSABLE),
    BILLING_RETURN_LINE_NOT_FOUND(ErrorCategory.UNPROCESSABLE),
    // Integracion de stock con Inventario (el movimiento de inventario rechazo la emision)
    BILLING_STOCK_INSUFFICIENT(ErrorCategory.UNPROCESSABLE),
    BILLING_STOCK_PRODUCT_NOT_AVAILABLE(ErrorCategory.UNPROCESSABLE),
    BILLING_STOCK_QUANTITY_NOT_SUPPORTED(ErrorCategory.UNPROCESSABLE),
    BILLING_STOCK_CONFIG_MISSING(ErrorCategory.UNPROCESSABLE),
    BILLING_STOCK_ORIGINAL_NOT_FOUND(ErrorCategory.UNPROCESSABLE),
    BILLING_STOCK_PERIOD_CLOSED(ErrorCategory.UNPROCESSABLE),
    BILLING_STOCK_REJECTED(ErrorCategory.UNPROCESSABLE),

    // BAD_REQUEST
    BILLING_INVALID_LINE(ErrorCategory.BAD_REQUEST),
    BILLING_ANNUL_REASON_REQUIRED(ErrorCategory.BAD_REQUEST),
    BILLING_VALIDATION_ERROR(ErrorCategory.BAD_REQUEST),

    // FORBIDDEN / UNAVAILABLE / INTERNAL
    BILLING_FORBIDDEN(ErrorCategory.FORBIDDEN),
    BILLING_SEQUENCE_BUSY(ErrorCategory.UNAVAILABLE),
    BILLING_STOCK_UNAVAILABLE(ErrorCategory.UNAVAILABLE),
    BILLING_INTERNAL_ERROR(ErrorCategory.INTERNAL);

    /** Categoria semantica del error (independiente de HTTP). */
    public enum ErrorCategory {
        NOT_FOUND,
        CONFLICT,
        UNPROCESSABLE,
        BAD_REQUEST,
        FORBIDDEN,
        UNAVAILABLE,
        INTERNAL
    }

    private final ErrorCategory category;

    BillingErrorCode(ErrorCategory category) {
        this.category = category;
    }

    public ErrorCategory category() {
        return category;
    }

    /** Codigo textual expuesto en el cuerpo de error. */
    public String code() {
        return name();
    }
}
