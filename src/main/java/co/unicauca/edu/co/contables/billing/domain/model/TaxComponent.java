package co.unicauca.edu.co.contables.billing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Un impuesto del desglose de una linea: nombre tal como esta en el maestro de impuestos (codigo,
 * p. ej. "Iva", "Ica") y su tarifa en porcentaje, escala 2 HALF_UP. Es SOLO presentacion congelada:
 * el impuesto de la linea se sigue calculando con {@link DocumentLine#taxRate()} (la suma), nunca
 * sumando estos componentes, para no cambiar el redondeo vigente.
 */
public record TaxComponent(String name, BigDecimal rate) {

    private static final int SCALE = 2;

    public TaxComponent {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del impuesto es obligatorio");
        }
        Objects.requireNonNull(rate, "rate");
        name = name.trim();
        rate = rate.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static TaxComponent of(String name, BigDecimal rate) {
        return new TaxComponent(name, rate);
    }

    /** Copia inmutable; {@code null} se normaliza a lista vacia (documentos sin desglose guardado). */
    public static List<TaxComponent> copyOf(List<TaxComponent> taxes) {
        return taxes == null ? List.of() : List.copyOf(taxes);
    }
}
