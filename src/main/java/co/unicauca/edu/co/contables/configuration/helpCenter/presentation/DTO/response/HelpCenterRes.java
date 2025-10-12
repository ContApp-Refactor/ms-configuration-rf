package co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenterRes {
    private Long id;
    private Integer moduleId;
    private String moduleName;
    private String name;
    private String description;
    private String idEnterprise;
    private Boolean status;
}
