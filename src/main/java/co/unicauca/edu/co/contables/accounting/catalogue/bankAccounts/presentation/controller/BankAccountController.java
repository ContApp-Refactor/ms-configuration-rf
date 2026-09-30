package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.mapper.BankAccountDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.response.BankAccountRes;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @brief Controlador REST para gestión de cuentas bancarias
 *
 * Expone endpoints HTTP para operaciones CRUD de cuentas bancarias
 * con soporte para filtros, paginación y búsqueda.
 */
@RestController
@RequestMapping("/api/accountCatalogue/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final IBankAccountService service;
    private final BankAccountDomainMapper mapper;

    @PreAuthorize("hasAuthority('BA#C')")
    @PostMapping("/create")
    public ResponseEntity<BankAccountRes> create(@Valid @RequestBody BankAccountCreateReq request) {
        BankAccount created = service.create(request);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @PreAuthorize("hasAuthority('BA#U')")
    @PutMapping("/update")
    public ResponseEntity<BankAccountRes> update(@Valid @RequestBody BankAccountUpdateReq request) {
        BankAccount updated = service.update(request);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @GetMapping("/findById/{id}/{enterpriseId}")
    public ResponseEntity<BankAccountRes> getById(@PathVariable Long id, @PathVariable String enterpriseId) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id, enterpriseId)));
    }

    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<Page<BankAccountRes>> list(
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
    public ResponseEntity<Page<BankAccountRes>> listActive(
            @PathVariable String enterpriseId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(service.findAllActiveByEnterprise(enterpriseId, page, size)
                .map(mapper::toRes));
    }


    @PreAuthorize("hasAuthority('BA#CS')")
    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<BankAccountRes> changeState(
            @PathVariable Long id, 
            @PathVariable String enterpriseId,
            @RequestParam Boolean state) {
        BankAccount updated = service.changeState(id, enterpriseId, state);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @PreAuthorize("hasAuthority('BA#D')")
    @DeleteMapping("/delete/{id}/{enterpriseId}")
    public ResponseEntity<BankAccountRes> delete(
            @PathVariable Long id, 
            @PathVariable String enterpriseId) {
        BankAccount deleted = service.delete(id, enterpriseId);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }
}
