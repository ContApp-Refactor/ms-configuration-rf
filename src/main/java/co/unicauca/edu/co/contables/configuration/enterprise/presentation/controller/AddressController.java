package co.unicauca.edu.co.contables.configuration.enterprise.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.AddressDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IAddressService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CitiesByDepartmentRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CountryRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.DepartmentRes;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST para países, departamentos y ciudades usados en la
 * ubicación de una empresa.
 */
@RequestMapping("/api/enterprises/address")
@RestController
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService service;
    private final AddressDomainMapper mapper;

    @GetMapping("/countries")
    @Operation(summary = "Obtener todos los países")
    public ResponseEntity<List<CountryRes>> getAllCountries() {
        return ResponseEntity.ok(mapper.toCountryResList(service.getAllCountries()));
    }

    @GetMapping("/countries/{idCountry}/departments")
    @Operation(summary = "Obtener los departamentos de un país")
    public ResponseEntity<List<DepartmentRes>> getDepartmentsByCountry(@PathVariable("idCountry") Long idCountry) {
        return ResponseEntity.ok(mapper.toDepartmentResList(service.getAllDepartments(idCountry)));
    }

    @GetMapping("/departments/{idDepartment}/cities")
    @Operation(summary = "Obtener ciudades por departamento")
    public ResponseEntity<CitiesByDepartmentRes> getAllCities(@PathVariable("idDepartment") Long idDepartment) {
        return ResponseEntity.ok(mapper.toCitiesByDepartmentRes(service.getAllCities(idDepartment)));
    }
}
