package co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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


