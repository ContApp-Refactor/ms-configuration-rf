package co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** @brief DTO de respuesta para módulos de documentos */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentModuleRes {
    
    private Integer id;
    private String name;
}
