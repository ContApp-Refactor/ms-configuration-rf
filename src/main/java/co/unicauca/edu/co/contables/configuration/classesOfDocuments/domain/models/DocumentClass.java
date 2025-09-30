package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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


