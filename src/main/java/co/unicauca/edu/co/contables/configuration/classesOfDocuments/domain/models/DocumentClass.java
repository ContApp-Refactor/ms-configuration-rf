package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Modelo de dominio para clases de documento
 *
 * Representa el concepto de clase de documento en la capa de dominio,
 * conteniendo la información básica de nombre, empresa y estado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentClass {
    private Long id;
    private String name;
    private String idEnterprise;
    @Builder.Default
    private Boolean status = true;
}


