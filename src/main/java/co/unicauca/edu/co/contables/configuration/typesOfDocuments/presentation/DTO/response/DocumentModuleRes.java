package co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para información de módulos de documentos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentModuleRes {
    
    private Integer id;
    private String name;
}
