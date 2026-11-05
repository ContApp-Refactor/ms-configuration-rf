package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.controller;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.*;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdTypeCreateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdTypeUpdateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.TypeIdCreateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.TypeIdUpdateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ThirdTypeResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper.IdRestMapper;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils.PaginationHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @brief Controlador REST para configuración de maestros de terceros
 *
 * Adaptador de entrada que expone endpoints para gestionar los datos maestros
 * del sistema de terceros: tipos de tercero y tipos de identificación.
 * Traduce entre el protocolo HTTP y los casos de uso del dominio.
 */
@RestController
@RequestMapping("/api/thirds/configuration")
@RequiredArgsConstructor
@Validated
public class ThirdConfigurationRestController {

    private final CreateThirdTypeUseCase createThirdTypeUseCase;
    private final ListThirdTypeUseCase listThirdTypeUseCase;
    private final UpdateThirdTypeUseCase updateThirdTypeUseCase;
    private final DeleteThirdTypeUseCase deleteThirdTypeUseCase;
    private final CreateTypeIdUseCase createTypeIdUseCase;
    private final ListTypeIdUseCase listTypeIdUseCase;
    private final UpdateTypeIdUseCase updateTypeIdUseCase;
    private final DeleteTypeIdUseCase deleteTypeIdUseCase;

    private final IdRestMapper idRestMapper;

    @PreAuthorize("hasAuthority('TT#C')")
    @PostMapping("/thirdtype")
    public ResponseEntity<ThirdTypeResponse> createThirdType(
            @RequestBody @Valid ThirdTypeCreateRequest thirdTypeCreateRequest) {

        ThirdType thirdType = idRestMapper.toThirdType(thirdTypeCreateRequest);
        ThirdType createdThirdType = createThirdTypeUseCase.createThirdType(thirdType);

        return new ResponseEntity<>(idRestMapper.toThirdTypeResponse(createdThirdType), HttpStatus.CREATED);
    }

    @GetMapping("/thirdtype")
    public ResponseEntity<Page<ThirdType>> getThirdType(
            @NotBlank(message = "entId es requerido") @RequestParam String entId,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {

        // Contar total de registros (con o sin filtro)
        long totalRecords = (search != null && !search.trim().isEmpty())
            ? listThirdTypeUseCase.countThirdTypesByEntIdAndSearch(entId, search)
            : listThirdTypeUseCase.countThirdTypesByEntId(entId);

        // Crear Pageable flexible
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Obtener página de datos (con o sin filtro) - siempre ordenado por ttName
        Page<ThirdType> page = (search != null && !search.trim().isEmpty())
            ? listThirdTypeUseCase.findThirdTypesByEntIdAndSearch(entId, search, pageable.getPageNumber(),
                    pageable.getPageSize(), "ttName", sortOrder)
            : listThirdTypeUseCase.getAllThirdTypesWithSort(entId, pageable.getPageNumber(),
                    pageable.getPageSize(), "ttName", sortOrder);

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TT#U') or hasAuthority('TT#CS')")
    @PostMapping("/thirdtype/update")
    public ResponseEntity<ThirdTypeResponse> updateThirdType(@RequestBody @Valid ThirdTypeUpdateRequest thirdTypeUpdateRequest) {
        ThirdType thirdType = idRestMapper.toThirdType(thirdTypeUpdateRequest);
        ThirdType updatedThirdType = updateThirdTypeUseCase.updateThirdType(thirdType);

        return new ResponseEntity<>(idRestMapper.toThirdTypeResponse(updatedThirdType), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TI#C')")
    @PostMapping("/typeid")
    public ResponseEntity<TypeId> createTypeId(@RequestBody @Valid TypeIdCreateRequest typeIdCreateRequest) {
        TypeId typeId = idRestMapper.toTypeId(typeIdCreateRequest);
        typeId = createTypeIdUseCase.createTypeId(typeId);

        return new ResponseEntity<>(typeId, HttpStatus.CREATED);
    }

    @GetMapping("/typeid")
    public ResponseEntity<Page<TypeId>> ListTypeId(
            @NotBlank(message = "entId es requerido") @RequestParam String entId,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "tiName") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {

        // Contar total de registros (con o sin filtro)
        long totalRecords = (search != null && !search.trim().isEmpty())
            ? listTypeIdUseCase.countByEntIdAndSearch(entId, search)
            : listTypeIdUseCase.countByEntId(entId);

        // Crear Pageable flexible
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Obtener página de datos (con o sin filtro)
        Page<TypeId> page = (search != null && !search.trim().isEmpty())
            ? listTypeIdUseCase.findByEntIdAndSearch(entId, search, pageable.getPageNumber(),
                    pageable.getPageSize(), sortField, sortOrder)
            : listTypeIdUseCase.getAllTypeIdsWithSort(entId, pageable.getPageNumber(),
                    pageable.getPageSize(), sortField, sortOrder);

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TI#U') or hasAuthority('TI#CS')")
    @PostMapping("/typeid/update")
    public ResponseEntity<TypeId> updateTypeId(@RequestBody @Valid TypeIdUpdateRequest typeIdUpdateRequest) {
        TypeId typeId = idRestMapper.toTypeId(typeIdUpdateRequest);
        typeId = updateTypeIdUseCase.updateTypeId(typeId);

        return new ResponseEntity<>(typeId, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TT#D')")
    @DeleteMapping("/thirdtype/delete")
    public ResponseEntity<Boolean> deleteThirdType(
            @NotNull(message = "Third type ID not be empty") @RequestParam("thirdTypeId") Long thirdTypeId,
            @NotNull(message = "Enterprise ID not be empty") @RequestParam("entId") String entId) {

        boolean deleted = deleteThirdTypeUseCase.deleteThirdType(thirdTypeId, entId);

        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TI#D')")
    @DeleteMapping("/typeid/delete")
    public ResponseEntity<Boolean> deleteTypeId(
            @NotNull(message = "Type ID not be empty") @RequestParam("typeIdId") Long typeIdId,
            @NotNull(message = "Enterprise ID not be empty") @RequestParam("entId") String entId) {

        boolean deleted = deleteTypeIdUseCase.deleteTypeId(typeIdId, entId);

        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    @GetMapping("/typeid-active")
    public ResponseEntity<Page<TypeId>> getActiveTypeId(
            @NotBlank(message = "entId es requerido") @RequestParam String entId,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size) {

        // Contar total de registros activos
        long totalRecords = listTypeIdUseCase.countActiveByEntId(entId);

        // Crear Pageable flexible
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Obtener página de datos activos ordenados por defecto (tiName asc)
        Page<TypeId> page = listTypeIdUseCase.getAllActiveTypeIds(entId, pageable.getPageNumber(),
                pageable.getPageSize());

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping("/thirdtype-active")
    public ResponseEntity<Page<ThirdType>> getActiveThirdType(
            @NotBlank(message = "entId es requerido") @RequestParam String entId,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size) {

        // Contar total de registros activos
        long totalRecords = listThirdTypeUseCase.countActiveThirdTypesByEntId(entId);

        // Crear Pageable flexible
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Obtener página de datos activos ordenados por defecto (ttName asc)
        Page<ThirdType> page = listThirdTypeUseCase.getAllActiveThirdTypes(entId, pageable.getPageNumber(),
                pageable.getPageSize());

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

}
