package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.controller;

import co.unicauca.edu.co.contables.billing.application.ports.input.ListPaymentCatalogUseCase;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingPaymentMethodResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.support.BillingPermissions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Selectores de pago de la factura de venta (solo lectura): metodos de pago activos de la empresa.
 * Permiso de consulta de la factura de venta (BFV#R).
 */
@RestController("billingPaymentCatalogController")
@RequestMapping(BillingPaymentCatalogController.BASE_PATH)
public class BillingPaymentCatalogController {

    public static final String BASE_PATH = "/api/billing/payment-catalog";

    private final ListPaymentCatalogUseCase useCase;

    public BillingPaymentCatalogController(ListPaymentCatalogUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/methods")
    @PreAuthorize(BillingPermissions.SALE_INVOICE_READ)
    public ResponseEntity<List<BillingPaymentMethodResponse>> methods(
            @RequestParam("enterpriseId") @NotBlank @Size(max = 50) String enterpriseId) {
        return ResponseEntity.ok(useCase.paymentMethods(enterpriseId).stream()
                .map(m -> new BillingPaymentMethodResponse(String.valueOf(m.id()), m.name()))
                .toList());
    }
}
