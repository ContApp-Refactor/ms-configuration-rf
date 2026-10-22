package co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio que representa el rango del nivel de detalle.
 *
 * Define los límites inferior y superior utilizados para filtrar cuentas
 * según el nivel seleccionado (clase, grupo, cuenta, subcuenta o cuenta
 * auxiliar) al momento de generar un libro auxiliar.
 *
 * Externamente el contrato JSON expone las propiedades como {@code from}
 * y {@code to}, alineadas con el modelo del front-end. Internamente los
 * campos conservan el sufijo {@code Range} para evitar colisiones con
 * palabras reservadas y mantener la legibilidad del dominio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaRange {

    @JsonProperty("from")
    @JsonAlias({"fromRange"})
    private Long fromRange;

    @JsonProperty("to")
    @JsonAlias({"toRange"})
    private Long toRange;

    public boolean isSingleValue() {
        return fromRange != null && fromRange.equals(toRange);
    }

}
