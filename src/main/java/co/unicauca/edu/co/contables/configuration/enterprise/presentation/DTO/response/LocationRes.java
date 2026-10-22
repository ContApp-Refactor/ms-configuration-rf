package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationRes {
    private Long id;
    private String address;
    private CityRes city;
    private DepartmentRes department;
    private CountryRes country;
}
