package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.identifiers;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

/**
 * @brief Clave primaria compuesta @Embeddable para thirds_and_types
 */
@Data
@Embeddable
public class ThirdsAndTypeId implements Serializable{
    private Long thId;
    private Long ttId;
}
