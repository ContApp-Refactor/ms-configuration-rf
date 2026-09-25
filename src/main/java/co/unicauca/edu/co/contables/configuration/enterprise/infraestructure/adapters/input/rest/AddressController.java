package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IAddressSearchManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.CitiesbyDepartmentResponse;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ICitiesbyDepartmentRestMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IDepartmentRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gestionar las operaciones relacionadas con direcciones.
 * Proporciona endpoints para obtener información de departamentos y ciudades.
 */
@RequestMapping("/api/enterprises/address")
@RestController
@AllArgsConstructor
public class AddressController {

    private final IAddressSearchManagerPort addressSearchManagerPort;
    private final ICitiesbyDepartmentRestMapper citiesbyDepartmentRestMapper;
    private final IDepartmentRestMapper departmentRestMapper;

    /**
     * Obtiene todos los departamentos disponibles en el sistema.
     *
     * @return ResponseEntity con la lista de departamentos
     */
    @GetMapping("/departments")
    @Operation(summary = "Obtener todos los departamentos", 
               description = "Recupera una lista de todos los departamentos disponibles en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", 
                        description = "Departamentos recuperados exitosamente", 
                        content = @Content(mediaType = "application/json", 
                        schema = @Schema(implementation = DepartmentAddressResponse.class))),
            @ApiResponse(responseCode = "500", 
                        description = "Error interno al recuperar los departamentos", 
                        content = @Content)
    })
    public ResponseEntity<List<DepartmentAddressResponse>> getAllDepartment() {
        // Recuperar la lista de departamentos a través del puerto de gestión de
        // direcciones
        List<Department> departments = addressSearchManagerPort.getAllDepartment();

        // Mapear los departamentos a un DTO y devolver la respuesta con estado HTTP 200
        return ResponseEntity.ok(departmentRestMapper.toDepartmentResponseList(departments));
    }

    /**
     * Obtiene todas las ciudades asociadas a un departamento específico.
     *
     * @param idDepartment el identificador del departamento
     * @return ResponseEntity con la información de las ciudades del departamento
     */
    @GetMapping("/cities/{idDepartment}")
    @Operation(summary = "Obtener ciudades por departamento", 
               description = "Recupera todas las ciudades asociadas a un departamento específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", 
                        description = "Ciudades recuperadas exitosamente", 
                        content = @Content(mediaType = "application/json", 
                        schema = @Schema(implementation = CitiesbyDepartmentResponse.class))),
            @ApiResponse(responseCode = "404", 
                        description = "Departamento no encontrado", 
                        content = @Content),
            @ApiResponse(responseCode = "500", 
                        description = "Error interno al recuperar las ciudades", 
                        content = @Content)
    })
    public ResponseEntity<CitiesbyDepartmentResponse> getAllCities(
            @PathVariable("idDepartment") Long idDepartment) {
        // Obtener el departamento junto con sus ciudades
        Department department = addressSearchManagerPort.getAllCities(idDepartment);
        
        // Mapear el departamento a una respuesta específica para ciudades
        CitiesbyDepartmentResponse citiesbyDepartmentResponse = 
                citiesbyDepartmentRestMapper.toResponse(department);
        
        // Retornar la respuesta con estado HTTP 200
        return ResponseEntity.ok(citiesbyDepartmentResponse);
    }
}
