package co.unicauca.edu.co.contables.billing.domain.exception;

import java.util.Objects;

/** Excepcion de negocio del modulo billing. */
public class BillingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final BillingErrorCode errorCode;

    public BillingException(BillingErrorCode errorCode, String detail) {
        super(detail == null || detail.isBlank() ? Objects.requireNonNull(errorCode).code() : detail);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    public BillingException(BillingErrorCode errorCode) {
        this(errorCode, null);
    }

    public BillingErrorCode errorCode() {
        return errorCode;
    }

    public BillingErrorCode.ErrorCategory category() {
        return errorCode.category();
    }
}
