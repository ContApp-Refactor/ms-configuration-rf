package co.unicauca.edu.co.contables.billing.domain.model;

/** Tipo de documento: factura o devolucion, de venta o de compra, con su prefijo de numeracion. */
public enum DocumentType {
    SALE_INVOICE(DocumentKind.SALE, false, "FV"),
    PURCHASE_INVOICE(DocumentKind.PURCHASE, false, "FC"),
    SALE_RETURN(DocumentKind.SALE, true, "NC"),
    PURCHASE_RETURN(DocumentKind.PURCHASE, true, "DC");

    private final DocumentKind kind;
    private final boolean returnDocument;
    private final String prefix;

    DocumentType(DocumentKind kind, boolean returnDocument, String prefix) {
        this.kind = kind;
        this.returnDocument = returnDocument;
        this.prefix = prefix;
    }

    public DocumentKind kind() {
        return kind;
    }

    public boolean isReturn() {
        return returnDocument;
    }

    public boolean isInvoice() {
        return !returnDocument;
    }

    public String prefix() {
        return prefix;
    }

    /** Tipo de devolucion que aplica a esta factura. Solo valido para facturas. */
    public DocumentType returnType() {
        switch (this) {
            case SALE_INVOICE:
                return SALE_RETURN;
            case PURCHASE_INVOICE:
                return PURCHASE_RETURN;
            default:
                throw new IllegalStateException(this + " no es una factura");
        }
    }

    /** Tipo de factura original de esta devolucion. Solo valido para devoluciones. */
    public DocumentType originType() {
        switch (this) {
            case SALE_RETURN:
                return SALE_INVOICE;
            case PURCHASE_RETURN:
                return PURCHASE_INVOICE;
            default:
                throw new IllegalStateException(this + " no es una devolucion");
        }
    }
}
