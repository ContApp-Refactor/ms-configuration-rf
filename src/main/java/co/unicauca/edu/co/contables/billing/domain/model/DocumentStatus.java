package co.unicauca.edu.co.contables.billing.domain.model;

/** Estados del documento (RN-01): BORRADOR -> EMITIDA -> ANULADA. */
public enum DocumentStatus {
    BORRADOR,
    EMITIDA,
    ANULADA;

    public boolean canTransitionTo(DocumentStatus target) {
        switch (this) {
            case BORRADOR:
                return target == EMITIDA;
            case EMITIDA:
                return target == ANULADA;
            default:
                return false;
        }
    }
}
