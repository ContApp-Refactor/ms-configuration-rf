package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.controller;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ListGeographyUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.CityResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.CountryResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.StateResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper.GeographyRestMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @brief Controlador REST para consultas de información geográfica
 *
 * Adaptador de entrada que expone endpoints para consultar datos geográficos
 * (países, estados/departamentos, ciudades) siguiendo la jerarquía geográfica.
 * Traduce entre el protocolo HTTP y los casos de uso del dominio.
 */
@RestController
@RequestMapping("/api/thirds/geography")
@RequiredArgsConstructor
@Validated
public class GeographyRestController {

    private final ListGeographyUseCase listGeographyUseCase;
    private final GeographyRestMapper geographyRestMapper;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponse>> getAllCountries() {
        List<Country> countries = listGeographyUseCase.getAllCountries();
        List<CountryResponse> response = geographyRestMapper.toCountryResponseList(countries);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/states")
    public ResponseEntity<List<StateResponse>> getStatesByCountry(
            @NotBlank(message = "El código del país es obligatorio")
            @RequestParam String countryCode) {

        List<State> states = listGeographyUseCase.getStatesByCountry(countryCode);
        List<StateResponse> response = geographyRestMapper.toStateResponseList(states);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityResponse>> getCitiesByState(
            @NotBlank(message = "El código del estado es obligatorio")
            @RequestParam String stateCode,
            @NotBlank(message = "El código del país es obligatorio")
            @RequestParam String countryCode) {

        List<City> cities = listGeographyUseCase.getCitiesByState(stateCode, countryCode);
        List<CityResponse> response = geographyRestMapper.toCityResponseList(cities);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
