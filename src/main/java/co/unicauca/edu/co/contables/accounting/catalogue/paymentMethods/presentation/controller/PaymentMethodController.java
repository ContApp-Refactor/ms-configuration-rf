package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.mapper.PaymentMethodDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.response.PaymentMethodRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @brief Controlador REST para gestión de métodos de pago
 *
 * Expone endpoints HTTP para operaciones CRUD de métodos de pago
 * con soporte para filtros, paginación y búsqueda por nombre o cuenta contable.
 */
@RestController
@RequestMapping("/api/accountCatalogue/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final IPaymentMethodService service;
    private final PaymentMethodDomainMapper mapper;

    @PreAuthorize("hasAuthority('PM#C')")
    @PostMapping("/create")
    public ResponseEntity<PaymentMethodRes> create(@Valid @RequestBody PaymentMethodCreateReq request) {
        PaymentMethod created = service.create(request);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @PreAuthorize("hasAuthority('PM#U')")
    @PutMapping("/update")
    public ResponseEntity<PaymentMethodRes> update(@Valid @RequestBody PaymentMethodUpdateReq request) {
        PaymentMethod updated = service.update(request);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @GetMapping("/findById/{id}/{enterpriseId}")
    public ResponseEntity<PaymentMethodRes> getById(@PathVariable Long id, @PathVariable String enterpriseId) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id, enterpriseId)));
    }

    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<Page<PaymentMethodRes>> list(
            @PathVariable String enterpriseId,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.findAllByEnterprise(enterpriseId, page, size, sortField, sortOrder, search)
                .map(mapper::toRes));
    }

    @GetMapping("/findAllActive/{enterpriseId}")
    public ResponseEntity<Page<PaymentMethodRes>> findAllActive(
            @PathVariable String enterpriseId,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size) {
        return ResponseEntity.ok(service.findAllActiveByEnterprise(enterpriseId, page, size)
                .map(mapper::toRes));
    }

    @PreAuthorize("hasAuthority('PM#CS')")
    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<PaymentMethodRes> changeState(
            @PathVariable Long id, 
            @PathVariable String enterpriseId,
            @RequestParam Boolean state) {
        PaymentMethod updated = service.changeState(id, enterpriseId, state);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @PreAuthorize("hasAuthority('PM#D')")
    @DeleteMapping("/delete/{id}/{enterpriseId}")
    public ResponseEntity<PaymentMethodRes> delete(
            @PathVariable Long id, 
            @PathVariable String enterpriseId) {
        PaymentMethod deleted = service.delete(id, enterpriseId);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }
}
