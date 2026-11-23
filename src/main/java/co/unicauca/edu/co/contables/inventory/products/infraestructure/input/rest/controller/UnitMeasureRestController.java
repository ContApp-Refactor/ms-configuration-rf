package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.UnitOfMeasureCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.UnitOfMeasureResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.IUnitOfMeasureRestMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.PaginationHelper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @brief Controlador REST para gestión de unidades de medida
 *
 *        Administra unidades de medida con operaciones CRUD completas,
 *        incluyendo validaciones de unicidad y controles de eliminación segura.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/unit-measures")
public class UnitMeasureRestController {

        private final IUnitOfMeasureServicePort unitOfMeasureServicePort;
        private final IUnitOfMeasureRestMapper unitOfMeasureRestMapper;

        @GetMapping("/findAll")
        public ResponseEntity<Page<UnitOfMeasureResponse>> findAll(
                        @RequestParam String enterpriseId,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) Optional<Integer> numPage,
                        @RequestParam(required = false) Optional<Integer> size,
                        @RequestParam(defaultValue = "name") String sortField,
                        @RequestParam(defaultValue = "asc") String sortOrder) {

                long totalRecords = (search != null && !search.trim().isEmpty())
                                ? unitOfMeasureServicePort.countByEntIdAndSearch(enterpriseId, search)
                                : unitOfMeasureServicePort.countAllUnitOfMeasuresByEntId(enterpriseId);

                Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

                Page<UnitOfMeasure> page = (search != null && !search.trim().isEmpty())
                                ? unitOfMeasureServicePort.findByEntIdAndSearch(enterpriseId, search,
                                                pageable.getPageNumber(), pageable.getPageSize(), sortField, sortOrder)
                                : unitOfMeasureServicePort.getAllUnitOfMeasuresByWithSort(enterpriseId,
                                                pageable.getPageNumber(), pageable.getPageSize(), sortField, sortOrder);

                Page<UnitOfMeasureResponse> response = page.map(unitOfMeasureRestMapper::toUnitOfMeasureResponse);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/findActivate")
        public ResponseEntity<Page<UnitOfMeasureResponse>> findActivate(
                        @RequestParam String enterpriseId,
                        @RequestParam(required = false) Optional<Integer> numPage,
                        @RequestParam(required = false) Optional<Integer> size) {

                long totalRecords = unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(enterpriseId);

                Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

                Page<UnitOfMeasure> page = unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(enterpriseId,
                                pageable.getPageNumber(), pageable.getPageSize());

                Page<UnitOfMeasureResponse> response = page.map(unitOfMeasureRestMapper::toUnitOfMeasureResponse);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/findById/{id}")
        public UnitOfMeasureResponse findById(@PathVariable Long id, @RequestParam String enterpriseId) {
                return unitOfMeasureRestMapper.toUnitOfMeasureResponse(
                                unitOfMeasureServicePort.findByIdAndEnterpriseId(id, enterpriseId));
        }

        @PreAuthorize("hasAuthority('UM#C')")
        @PostMapping("/create")
        public ResponseEntity<UnitOfMeasureResponse> create(
                        @Valid @RequestBody UnitOfMeasureCreateRequest unitOfMeasureCreateRequest) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(unitOfMeasureRestMapper.toUnitOfMeasureResponse(
                                                unitOfMeasureServicePort
                                                                .create(unitOfMeasureRestMapper.toUnitOfMeasure(
                                                                                unitOfMeasureCreateRequest))));
        }

        @PreAuthorize("hasAuthority('UM#U')")
        @PutMapping("/update/{id}")
        public UnitOfMeasureResponse update(@PathVariable Long id,
                        @Valid @RequestBody UnitOfMeasureCreateRequest unitOfMeasureCreateRequest) {
                UnitOfMeasure unitOfMeasure = unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest);
                return unitOfMeasureRestMapper.toUnitOfMeasureResponse(
                                unitOfMeasureServicePort.update(id, unitOfMeasureCreateRequest.getEnterpriseId(),
                                                unitOfMeasure));
        }

        @PreAuthorize("hasAuthority('UM#CS')")
        @PutMapping("/changeState/{id}")
        public void changeState(@PathVariable Long id, @RequestParam String enterpriseId) {
                unitOfMeasureServicePort.changeState(id, enterpriseId);
        }

        @PreAuthorize("hasAuthority('UM#D')")
        @DeleteMapping("/delete/{id}")
        public void deleteById(@PathVariable Long id, @RequestParam String enterpriseId) {
                unitOfMeasureServicePort.deleteById(id, enterpriseId);
        }

}
