package co.unicauca.edu.co.contables.configuration.noCommercialTags.presentation.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagDTOResponse {
    private Long id;
    private String enterpriseId;
    private String title;
    private String description;
}
