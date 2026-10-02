package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductTypeServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductTypeRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.ProductTypeResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.IProductTypeRestMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.PaginationHelper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @brief Controlador REST para gestión de tipos de producto
 *
 * Proporciona endpoints CRUD para administración de tipos de producto,
 * incluyendo operaciones de cambio de estado y búsquedas paginadas.
 */
@RestController
@RequestMapping("/api/product-types")
@RequiredArgsConstructor // Use constructor injection
public class ProductTypeRestController {

    private final IProductTypeServicePort productTypeService;
    private final IProductTypeRestMapper productTypeMapper;

    @PreAuthorize("hasAuthority('PT#C')")
    @PostMapping
    public ResponseEntity<ProductTypeResponse> createProductType(@Valid @RequestBody ProductTypeRequest productTypeRequest) {
        ProductType productType = productTypeMapper.toProductType(productTypeRequest);
        ProductType createdProductType = productTypeService.createProductType(productType);
        ProductTypeResponse response = productTypeMapper.toProductTypeResponse(createdProductType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/findActivate")
    public ResponseEntity<Page<ProductTypeResponse>> findActivate(
            @RequestParam String enterpriseId,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size) {

        long totalRecords = productTypeService.countActivatedByEnterpriseId(enterpriseId);

        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        Page<ProductType> page = productTypeService.findActivatedWithPagination(enterpriseId,
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ProductTypeResponse> response = page.map(productTypeMapper::toProductTypeResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<ProductTypeResponse>> findAll(
            @RequestParam String enterpriseId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        long totalRecords = (search != null && !search.trim().isEmpty())
                ? productTypeService.countByEnterpriseIdAndSearch(enterpriseId, search)
                : productTypeService.countByEnterpriseId(enterpriseId);

        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        Page<ProductType> page = (search != null && !search.trim().isEmpty())
                ? productTypeService.findByEnterpriseIdAndSearch(enterpriseId, search,
                        pageable.getPageNumber(), pageable.getPageSize(), sortField, sortOrder)
                : productTypeService.getAllProductTypesByWithSort(enterpriseId,
                        pageable.getPageNumber(), pageable.getPageSize(), sortField, sortOrder);

        Page<ProductTypeResponse> response = page.map(productTypeMapper::toProductTypeResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeResponse> getProductTypeById(@PathVariable Long id, @RequestParam String enterpriseId) {
        ProductType productType = productTypeService.getProductTypeByIdAndEnterpriseId(id, enterpriseId);
        ProductTypeResponse response = productTypeMapper.toProductTypeResponse(productType);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('PT#U')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductTypeResponse> updateProductType(
            @PathVariable Long id,
            @Valid @RequestBody ProductTypeRequest productTypeRequest) {
        ProductType productType = productTypeMapper.toProductType(productTypeRequest);
        ProductType updatedProductType = productTypeService.updateProductType(id, productTypeRequest.getEnterpriseId(), productType);
        ProductTypeResponse response = productTypeMapper.toProductTypeResponse(updatedProductType);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('PT#CS')")
    @PutMapping("/changeState/{id}")
    public void changeState(@PathVariable Long id, @RequestParam String enterpriseId) {
        productTypeService.changeState(id, enterpriseId);
    }

    @PreAuthorize("hasAuthority('PT#D')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@PathVariable Long id, @RequestParam String enterpriseId) {
        productTypeService.deleteProductType(id, enterpriseId);
        return ResponseEntity.noContent().build();
    }
}
