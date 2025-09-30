package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {
    private Long id;
    private String prefix;
    private String name;
    private Long documentClassId;
    private String module;
    private String idEnterprise;
    @Builder.Default
    private Boolean status = true;
}
