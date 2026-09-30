package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums;

public enum ProcessingStatus {
    /**
     * El evento ha sido recibido pero los asientos contables aún no han sido generados.
     */
    PENDING,
    
    /**
     * Los asientos contables han sido generados y guardados exitosamente.
     */
    PROCESSED,
    
    /**
     * Ocurrió un error irrecuperable durante el procesamiento.
     * Este estado indica que requiere intervención manual.
     */
    FAILED,
    
    /**
     * El recibo original fue anulado y los asientos contables han sido revertidos.
     */
    VOIDED
}
