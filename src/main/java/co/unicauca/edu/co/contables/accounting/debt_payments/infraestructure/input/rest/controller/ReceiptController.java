package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IReceiptCommandUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IReceiptQueryUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.ReceiptCreateRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.VoidReceiptRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ApiResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ReceiptResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.IReceiptRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @brief REST controller for managing receipts, including creation, voiding,
 *        and retrieval
 *        Handles endpoints for creating receipts, voiding them, and fetching
 *        receipts by various criteria
 */

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class ReceiptController {
    private final IReceiptCommandUseCase receiptCommandUseCase;
    private final IReceiptQueryUseCase receiptQueryUseCase;
    private final IReceiptRestMapper receiptRestMapper;

    //@PreAuthorize("hasAuthority('Create_Receipt')")
    @PostMapping("/")
    public ResponseEntity<ApiResponse<ReceiptResponse>> createReceipt(
            @Valid @RequestBody ReceiptCreateRequest request) {
        Receipt domainModel = receiptRestMapper.toDomain(request);
        Receipt createdReceipt = receiptCommandUseCase.createReceipt(domainModel);
        ReceiptResponse responseDto = receiptRestMapper.toResponse(createdReceipt);

        // Mantenemos el espíritu del 201 Created, pero con nuestro wrapper.
        // El 'data' es el recibo creado.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto, "Recibo creado exitosamente."));
    }

    //@PreAuthorize("hasAuthority('Void_Receipt')")
    @PutMapping("/{id}/void")
    public ResponseEntity<ApiResponse<ReceiptResponse>> voidReceipt(@PathVariable Long id,
            @Valid @RequestBody VoidReceiptRequest request) {
        Receipt voidedReceipt = receiptCommandUseCase.voidReceipt(id, request.getReason());
        ReceiptResponse responseDto = receiptRestMapper.toResponse(voidedReceipt);

        // La operación fue exitosa, devolvemos un 200 OK con el recibo anulado.
        return ResponseEntity.ok(ApiResponse.success(responseDto, "Recibo anulado correctamente."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptById(@PathVariable Long id) {
        Receipt receipt = receiptQueryUseCase.findById(id);
        ReceiptResponse responseDto = receiptRestMapper.toResponse(receipt);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @GetMapping("/by-enterprise/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getAllReceiptsByEnterprise(
            @PathVariable String enterpriseId) {
        List<Receipt> receipts = receiptQueryUseCase.findByEnterpriseId(enterpriseId);

        if (receipts.isEmpty()) {
            // Reemplazamos 204 No Content por 200 OK con nuestro wrapper informativo.
            return ResponseEntity.ok(
                    ApiResponse.successEmpty("No se encontraron recibos para la empresa especificada.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(receiptRestMapper.toResponseList(receipts)));
    }

    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getAllReceiptsByInvoice(@PathVariable String invoiceId) {
        List<Receipt> receipts = receiptQueryUseCase.findByInvoiceId(invoiceId);

        if (receipts.isEmpty()) {
            return ResponseEntity.ok(
                    ApiResponse.successEmpty("No se encontraron recibos asociados a la factura.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(receiptRestMapper.toResponseList(receipts)));
    }

    @GetMapping("/by-third/{thirdId}/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getAllReceiptsByThird(@PathVariable String thirdId, @PathVariable String enterpriseId) {
        List<Receipt> receipts = receiptQueryUseCase.findByThirdPartyId(thirdId, enterpriseId);

        if (receipts.isEmpty()) {
            return ResponseEntity.ok(
                    ApiResponse.successEmpty("No se encontraron recibos asociados al tercero.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(receiptRestMapper.toResponseList(receipts)));
    }

}
