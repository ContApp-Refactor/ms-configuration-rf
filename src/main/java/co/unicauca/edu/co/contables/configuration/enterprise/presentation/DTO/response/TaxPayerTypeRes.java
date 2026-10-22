package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxPayerTypeRes {
    private Long id;
    private String name;
}
