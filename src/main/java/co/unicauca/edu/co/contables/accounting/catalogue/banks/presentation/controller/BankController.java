package co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.IBankService;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.mapper.BankDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.response.BankRes;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @brief Controlador REST para gestión de bancos
 *
 * Expone endpoints HTTP para operaciones CRUD de bancos
 * con soporte para filtros, paginación y búsqueda.
 */
@RestController
@RequestMapping("/api/accountCatalogue/banks")
@RequiredArgsConstructor
public class BankController {

    private final IBankService service;
    private final BankDomainMapper mapper;

    @PreAuthorize("hasAuthority('B#C')")
    @PostMapping("/create")
    public ResponseEntity<BankRes> create(@Valid @RequestBody BankCreateReq request) {
        Bank created = service.create(request);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @PreAuthorize("hasAuthority('B#U')")
    @PutMapping("/update")
    public ResponseEntity<BankRes> update(@Valid @RequestBody BankUpdateReq request) {
        Bank updated = service.update(request);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @GetMapping("/findById/{id}/{enterpriseId}")
    public ResponseEntity<BankRes> getById(@PathVariable Long id, @PathVariable String enterpriseId) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id, enterpriseId)));
    }

    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<Page<BankRes>> list(
            @PathVariable String enterpriseId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.findAllByEnterpriseWithFilters(enterpriseId, page, size, sortField, sortOrder, search)
                .map(mapper::toRes));
    }

    @GetMapping("/findAllActive/{enterpriseId}")
    public ResponseEntity<Page<BankRes>> listActive(
            @PathVariable String enterpriseId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(service.findAllActiveByEnterprise(enterpriseId, page, size)
                .map(mapper::toRes));
    }

    @PreAuthorize("hasAuthority('B#CS')")
    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<BankRes> changeState(
            @PathVariable Long id, 
            @PathVariable String enterpriseId,
            @RequestParam Boolean state) {
        Bank updated = service.changeState(id, enterpriseId, state);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @PreAuthorize("hasAuthority('B#D')")
    @DeleteMapping("/delete/{id}/{enterpriseId}")
    public ResponseEntity<BankRes> delete(
            @PathVariable Long id, 
            @PathVariable String enterpriseId) {
        Bank deleted = service.delete(id, enterpriseId);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }
}
