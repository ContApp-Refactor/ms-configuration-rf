package co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeRes {
    private Long id;
    private String idEnterprise;
    private String prefix;
    private String name;
    private Long documentClassId;
    private Integer moduleId;
    private Boolean status;
}


