package co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de respuesta para clases de documento
 *
 * Representa la estructura de datos enviada como respuesta en las operaciones
 * de clases de documento, incluyendo ID, nombre, empresa y estado.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentClassRes {
    private Long id;
    private String name;
    private String idEnterprise;
    private Boolean status;
}


