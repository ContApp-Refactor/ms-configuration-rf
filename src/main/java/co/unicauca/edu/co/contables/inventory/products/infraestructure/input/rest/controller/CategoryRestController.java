package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.CategoryCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.CategoryResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.ICategoryRestMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.PaginationHelper;

import java.util.Optional;

/**
 * @brief Controlador REST para gestión de categorías de productos
 *
 * Maneja operaciones CRUD de categorías con validaciones de integridad
 * referencial y controles de eliminación segura.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryRestController {

        private final ICategoryServicePort categoryServicePort;
        private final ICategoryRestMapper categoryRestMapper;

        @GetMapping("/findById/{enterpriseId}/{id}")
        public CategoryResponse findById(@PathVariable String enterpriseId, @PathVariable Long id) {
                return categoryRestMapper.toCategoryResponse(categoryServicePort.findById(enterpriseId, id));
        }

        @GetMapping("/findAll")
        public ResponseEntity<Page<CategoryResponse>> getCategoriesList(
                @RequestParam String enterpriseId,
                @RequestParam(required = false) Optional<Integer> numPage,
                @RequestParam(required = false) Optional<Integer> size,
                @RequestParam(defaultValue = "name") String sortField,
                @RequestParam(defaultValue = "asc") String sortOrder,
                @RequestParam(required = false) String search) {

                // Contar total de registros (con o sin filtro)
                long totalRecords = (search != null && !search.trim().isEmpty())
                        ? categoryServicePort.countByEntIdAndSearch(enterpriseId, search)
                        : categoryServicePort.countAllCategoriesByEntId(enterpriseId);

                // Crear Pageable flexible
                Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

                // Obtener página de datos (con o sin filtro)
                Page<Category> page = (search != null && !search.trim().isEmpty())
                        ? categoryServicePort.findByEntIdAndSearch(enterpriseId, search, pageable.getPageNumber(),
                                pageable.getPageSize(), sortField, sortOrder)
                        : categoryServicePort.getAllCategoriesByWithSort(enterpriseId, pageable.getPageNumber(),
                                pageable.getPageSize(), sortField, sortOrder);

                Page<CategoryResponse> responsePage = page.map(categoryRestMapper::toCategoryResponse);

                return new ResponseEntity<>(responsePage, HttpStatus.OK);
        }

        @GetMapping("/findActivate")
        public ResponseEntity<Page<CategoryResponse>> findActivate(
                @RequestParam String enterpriseId,
                @RequestParam(required = false) Optional<Integer> numPage,
                @RequestParam(required = false) Optional<Integer> size) {

                long totalRecords = categoryServicePort.countActiveCategoriesByEntId(enterpriseId);
                Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

                Page<Category> page = categoryServicePort.getAllActiveCategoriesByWithSort(enterpriseId, pageable.getPageNumber(),
                        pageable.getPageSize(), "name", "asc");

                Page<CategoryResponse> responsePage = page.map(categoryRestMapper::toCategoryResponse);

                return new ResponseEntity<>(responsePage, HttpStatus.OK);
        }

        @PreAuthorize("hasAuthority('C#C')")
        @PostMapping("/create")
        public ResponseEntity<CategoryResponse> create(
                        @Valid @RequestBody CategoryCreateRequest categoryCreateRequest) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(categoryRestMapper.toCategoryResponse(
                                                categoryServicePort.create(
                                                                categoryRestMapper.toCategory(categoryCreateRequest))));
        }

        @PreAuthorize("hasAuthority('C#U')")
        @PutMapping("/update/{enterpriseId}/{id}")
        public CategoryResponse update(@PathVariable String enterpriseId, @PathVariable Long id,
                        @Valid @RequestBody CategoryCreateRequest categoryCreateRequest) {
                return categoryRestMapper.toCategoryResponse(
                                categoryServicePort.update(enterpriseId, id, categoryRestMapper.toCategory(categoryCreateRequest)));
        }

        @PreAuthorize("hasAuthority('C#CS')")
        @PutMapping("/changeState/{enterpriseId}/{id}")
        public void changeState(@PathVariable String enterpriseId, @PathVariable Long id) {
                categoryServicePort.changeState(enterpriseId, id);
        }

        @PreAuthorize("hasAuthority('C#D')")
        @DeleteMapping("/delete/{enterpriseId}/{id}")
        public void deleteById(@PathVariable String enterpriseId, @PathVariable Long id) {
                categoryServicePort.deleteById(enterpriseId, id);
        }
}
