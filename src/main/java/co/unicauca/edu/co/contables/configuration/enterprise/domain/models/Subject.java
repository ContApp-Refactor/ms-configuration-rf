package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @brief Modelo de dominio para una materia (subject)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {
    private UUID id;
    private String code;
    private String name;
}
