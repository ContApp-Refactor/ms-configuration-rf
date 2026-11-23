package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonTypeRes {
    private Long id;
    private String type;
    private String name;
    private String surname;
    private String bussinessName;
}
