package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Modelo de dominio que representa una materia (subject).
 * Contiene los datos básicos de identificación y nombre.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    /**
     * Identificador único de la materia.
     */
    private UUID id;

    /**
     * Código de la materia.
     */
    private String code;

    /**
     * Nombre de la materia.
     */
    private String name;
}
