package co.unicauca.edu.co.contables.configuration.helpCenter.domain.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenter {
    private Long id;
    private Integer moduleId;
    private String name;
    private String description;
    private String idEnterprise;
    @Builder.Default
    private Boolean status = true;
}
