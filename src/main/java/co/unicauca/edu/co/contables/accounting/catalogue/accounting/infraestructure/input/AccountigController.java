package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountingSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.AccountingEntryResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.AccountingMovementResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ApiResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.mapper.IAccountingRestMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accountCatalogue/accounting")
@RequiredArgsConstructor
public class AccountigController {
    private final IAccountingSearchInputPort accountingSearchInputPort;
    private final IAccountingRestMapper accountingRestMapper;

    @GetMapping("/entries/{id}")
    public ResponseEntity<ApiResponse<AccountingEntryResponse>> getEntryById(@PathVariable Long id) {
        AccountingEntry entry = accountingSearchInputPort.findAccountingEntryById(id);
        AccountingEntryResponse response = accountingRestMapper.toEntryResponse(entry);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entries/by-receipt/{receiptId}")
    public ResponseEntity<ApiResponse<AccountingEntryResponse>> getEntryByReceiptId(@PathVariable Long receiptId) {
        AccountingEntry entry = accountingSearchInputPort.findAccountingEntryByReceiptId(receiptId);
        return ResponseEntity.ok(ApiResponse.success(accountingRestMapper.toEntryResponse(entry)));
    }

    @GetMapping("/entries/by-source/{sourceDocumentId}/{type}")
    public ResponseEntity<ApiResponse<AccountingEntryResponse>> getEntryBySourceDocumentIdAndType(
            @PathVariable Long sourceDocumentId, @PathVariable String type) {
        AccountingEntry entry = accountingSearchInputPort.findAccountingEntryBySourceDocumentIdAndType(sourceDocumentId,
                type);
        return ResponseEntity.ok(ApiResponse.success(accountingRestMapper.toEntryResponse(entry)));
    }

    @GetMapping("/movements/by-account/{accountId}")
    public ResponseEntity<ApiResponse<List<AccountingMovementResponse>>> getMovementsByAccount(
            @PathVariable Long accountId) {
        List<AccountingMovement> movements = accountingSearchInputPort.findMovementsByAccountId(accountId);

        if (movements.isEmpty()) {
            return ResponseEntity.ok(ApiResponse
                    .successEmpty("No se encontraron movimientos para la cuenta especificada.", "NO_CONTENT"));
        }

        List<AccountingMovementResponse> response = movements.stream()
                .map(accountingRestMapper::toMovementResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/movements/by-third-party/{thirdPartyId}")
    public ResponseEntity<ApiResponse<List<AccountingMovementResponse>>> getMovementsByThirdParty(
            @PathVariable Long thirdPartyId) {
        List<AccountingMovement> movements = accountingSearchInputPort.findMovementsByThirdPartyId(thirdPartyId);

        if (movements.isEmpty()) {
            return ResponseEntity.ok(ApiResponse
                    .successEmpty("No se encontraron movimientos para el tercero especificado.", "NO_CONTENT"));
        }

        List<AccountingMovementResponse> response = movements.stream()
                .map(accountingRestMapper::toMovementResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
