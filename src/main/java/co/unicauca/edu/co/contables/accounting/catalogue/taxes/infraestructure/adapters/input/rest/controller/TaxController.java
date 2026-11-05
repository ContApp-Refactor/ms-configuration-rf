package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxChangeStateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxCreateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxDeleteInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.request.TaxCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.request.TaxUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxChangeStateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxCreateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxSearchRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxUpdateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxChangeStateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxCreateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxSearchRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxUpdateRestMapper;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.unicauca.edu.co.contables.commons.utils.PaginationHelper;

import java.util.List;
import java.util.Optional;

/**
 * @brief Controlador REST para operaciones de impuestos
 *
 * Proporciona endpoints para crear, buscar, actualizar y eliminar impuestos,
 * así como para cambiar su estado activo/inactivo. También maneja paginación y
 * búsqueda flexible.
 */
@RequestMapping("/api/tax")
@RestController
@AllArgsConstructor
public class TaxController {

    private final ITaxCreateRestMapper taxCreateRestMapper;
    private final ITaxSearchRestMapper taxSearchRestMapper;
    private final ITaxCreateInputPort taxCreateInputPort;
    private final ITaxUpdateInputPort taxUpdateInputPort;
    private final ITaxSearchInputPort taxSearchInputPort;
    private final ITaxUpdateRestMapper taxUpdateRestMapper;
    private final ITaxDeleteInputPort taxDeleteInputPort;
    private final ITaxChangeStateInputPort taxChangeStateInputPort;
    private final ITaxChangeStateRestMapper taxChangeStateRestMapper;
    private final PaginationHelper paginationHelper;

    @PreAuthorize("hasAuthority('T#C')")
    @PostMapping("/")
    public ResponseEntity<TaxCreateRes> createTax(@RequestBody @Valid TaxCreateReq taxCreateReq) {
        TaxDTO taxDTO = taxCreateRestMapper.toDomain(taxCreateReq);
        Tax tax = taxCreateInputPort.createTax(taxDTO);
        return ResponseEntity.ok(taxCreateRestMapper.toCreateResponse(tax));
    }

    @GetMapping("/{code}/{idEnterprise}")
    public ResponseEntity<TaxSearchRes> getTax(@PathVariable String code, @PathVariable String idEnterprise) {
        Tax tax = taxSearchInputPort.getTax(code, idEnterprise);
        return ResponseEntity.ok(taxSearchRestMapper.toSearchResponse(tax));
    }

    @GetMapping("/taxes/{idEnterprise}")
    public ResponseEntity<Page<TaxSearchRes>> getTaxesPaginated(
            @PathVariable String idEnterprise,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "description") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {

        long totalRecords = (search != null && !search.trim().isEmpty())
                ? taxSearchInputPort.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search)
                : taxSearchInputPort.countTaxesByEnterprise(idEnterprise);

        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);

        Page<Tax> pageResult = (search != null && !search.trim().isEmpty())
                ? taxSearchInputPort.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, pageable.getPageNumber(),
                        pageable.getPageSize(), sortField, sortOrder)
                : taxSearchInputPort.getTaxesPaginated(idEnterprise, pageable.getPageNumber(),
                        pageable.getPageSize(), sortField, sortOrder);

        return ResponseEntity.ok(pageResult.map(taxSearchRestMapper::toSearchResponse));
    }

    @GetMapping("/active/{idEnterprise}")
    public ResponseEntity<List<TaxSearchRes>> getActiveTaxes(@PathVariable String idEnterprise) {
        List<Tax> activeTaxes = taxSearchInputPort.getActiveTaxes(idEnterprise);
        return ResponseEntity.ok(taxSearchRestMapper.toSearchListResponse(activeTaxes));
    }

    @PreAuthorize("hasAuthority('T#U')")
    @PutMapping("/{id}")
    public ResponseEntity<TaxUpdateRes> updateTax(@PathVariable long id, @RequestBody @Valid TaxUpdateReq taxUpdateReq) {
        TaxDTO taxDTO = taxUpdateRestMapper.toDomain(taxUpdateReq);
        Tax tax = taxUpdateInputPort.update(taxDTO, id);
        return ResponseEntity.ok(taxUpdateRestMapper.toCreateResponse(tax));
    }

    @PreAuthorize("hasAuthority('T#D')")
    @DeleteMapping("/{id}/{enterpriseId}")
    public ResponseEntity<String> deleteByCode(@PathVariable long id, @PathVariable String enterpriseId) {
        if (taxDeleteInputPort.deleteByCode(id, enterpriseId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Impuesto no encontrado para la empresa especificada");
        }
    }

    @PreAuthorize("hasAuthority('T#CS')")
    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<TaxChangeStateRes> changeState(
            @PathVariable Long id,
            @PathVariable String enterpriseId,
            @RequestParam Boolean status) {

        Tax updatedTax = taxChangeStateInputPort.changeState(id, enterpriseId, status);
        TaxChangeStateRes response = taxChangeStateRestMapper.toChangeStateResponse(updatedTax);
        return ResponseEntity.ok(response);
    }

}
