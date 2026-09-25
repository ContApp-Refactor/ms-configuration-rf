package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * @brief Clave compuesta para ThirdsAndTypesEntity (thId + ttId)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThirdsAndTypesId implements Serializable {

    private Long thId;
    private Long ttId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThirdsAndTypesId that = (ThirdsAndTypesId) o;
        return Objects.equals(thId, that.thId) && Objects.equals(ttId, that.ttId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thId, ttId);
    }
}
