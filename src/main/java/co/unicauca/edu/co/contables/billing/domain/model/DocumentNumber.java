package co.unicauca.edu.co.contables.billing.domain.model;

/** Numero consecutivo de un documento emitido: prefijo + 6 digitos (ej. FV-000123). */
public record DocumentNumber(String prefix, long value) {

    public DocumentNumber {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("El prefijo del numero es obligatorio");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("El consecutivo debe ser mayor que cero");
        }
    }

    public static DocumentNumber of(DocumentType type, long value) {
        return new DocumentNumber(type.prefix(), value);
    }

    public String display() {
        return String.format("%s-%06d", prefix, value);
    }

    @Override
    public String toString() {
        return display();
    }
}
