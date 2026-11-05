package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import lombok.Data;

import java.util.List;

/**
 * @brief Respuesta con un departamento y sus ciudades asociadas.
 */
@Data
public class CitiesByDepartmentRes {
    private Long id;
    private String name;
    private List<CityRes> cities;
}
