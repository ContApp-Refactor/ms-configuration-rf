package co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tag {
    private Long id;
    private String enterpriseId;
    private String title;
    private String description;

}
