package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IPortfolioWriteOffCommandUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IPortfolioWriteOffQueryUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.CreateWriteOffRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.WriteOffDetailRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ApiResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.InvoiceSummaryResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.PortfolioWriteOffResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.WriteOffDetailResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.IPortfolioWriteOffRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @brief REST controller for managing portfolio write-offs (debt write-offs)
 *        Handles endpoints for creating, confirming, voiding, and retrieving
 *        write-offs
 *        Also enriches responses with invoice details where applicable
 */

@RestController
@RequestMapping("/api/payments/write-offs")
@RequiredArgsConstructor
public class PortfolioWriteOffController {

    private final IPortfolioWriteOffCommandUseCase commandUseCase;
    private final IPortfolioWriteOffQueryUseCase queryUseCase;
    private final IPortfolioWriteOffRestMapper portfolioWriteOffRestMapper;
    private final IInvoiceProviderPort invoiceProviderPort;

    //@PreAuthorize("hasAuthority('Create_WriteOff')")
    @PostMapping("/")
    public ResponseEntity<ApiResponse<PortfolioWriteOffResponse>> createWriteOff(
            @Valid @RequestBody CreateWriteOffRequest request) {
        List<Long> invoiceIds = request.getDetails().stream().map(WriteOffDetailRequest::getInvoiceId).toList();
        Map<Long, InvoiceReplica> invoiceMap = invoiceProviderPort.findInvoicesByIds(invoiceIds).stream()
                .collect(Collectors.toMap(InvoiceReplica::getId, Function.identity()));

        if (invoiceMap.size() != invoiceIds.size()) {
            throw new InvoiceNotFoundException("Una o más de las facturas especificadas no existen.");
        }

        List<WriteOffDetail> domainDetails = request.getDetails().stream()
                .map(detailRequest -> {
                    InvoiceReplica invoice = invoiceMap.get(detailRequest.getInvoiceId());
                    return WriteOffDetail.builder()
                            .invoiceId(invoice.getId())
                            .amountWrittenOff(invoice.getPendingValue())
                            .accountingAccount(invoice.getAccountingAccount())
                            .build();
                })
                .collect(Collectors.toList());

        PortfolioWriteOff domainToCreate = PortfolioWriteOff.create(
                request.getEnterpriseId(), request.getThirdId(), request.getJustification(),
                domainDetails, request.getCostCenterId());

        domainToCreate.setWriteOffDate(request.getWriteOffDate());
        domainToCreate.setDebitAuxiliaryAccount(request.getDebitAuxiliaryAccount());
        domainToCreate.setDebitAuxiliaryAccountId(request.getDebitAuxiliaryAccountId());
        PortfolioWriteOff createdDomain = commandUseCase.createWriteOff(domainToCreate);

        PortfolioWriteOffResponse responseDto = buildEnrichedResponse(createdDomain);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto, "Castigo de cartera creado exitosamente."));
    }

    //@PreAuthorize("hasAuthority('Confirm_WriteOff')")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<PortfolioWriteOffResponse>> confirmWriteOff(@PathVariable Long id) {
        PortfolioWriteOff confirmedDomain = commandUseCase.confirmWriteOff(id);
        PortfolioWriteOffResponse responseDto = buildEnrichedResponse(confirmedDomain);
        return ResponseEntity.ok(ApiResponse.success(responseDto, "Castigo de cartera confirmado."));
    }

    //@PreAuthorize("hasAuthority('Void_WriteOff')")
    @PutMapping("/{id}/void")
    public ResponseEntity<ApiResponse<PortfolioWriteOffResponse>> voidWriteOff(@PathVariable Long id) {
        PortfolioWriteOff voidedDomain = commandUseCase.voidWriteOffConfirmation(id);
        PortfolioWriteOffResponse responseDto = buildEnrichedResponse(voidedDomain);
        return ResponseEntity.ok(ApiResponse.success(responseDto, "Confirmación de castigo de cartera anulada."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PortfolioWriteOffResponse>> getWriteOffById(@PathVariable Long id) {
        PortfolioWriteOff writeOff = queryUseCase.findById(id);
        PortfolioWriteOffResponse responseDto = buildEnrichedResponse(writeOff);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @GetMapping("/by-enterprise/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<PortfolioWriteOffResponse>>> getByEnterprise(@PathVariable String enterpriseId) {
        List<PortfolioWriteOff> domainList = queryUseCase.findByEnterpriseId(enterpriseId);

        if (domainList.isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.successEmpty("No se encontraron castigos de cartera para la empresa.", "NO_CONTENT")
            );
        }

        List<PortfolioWriteOffResponse> responseList = domainList.stream()
                .map(this::buildEnrichedResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    @GetMapping("/confirmed-or-voided/by-enterprise/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<PortfolioWriteOffResponse>>> getConfirmedOrVoidedByEnterprise(@PathVariable String enterpriseId) {
        List<PortfolioWriteOff> domainList = queryUseCase.findConfirmedOrVoidedByEnterpriseId(enterpriseId);

        if (domainList.isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.successEmpty("No se encontraron castigos de cartera confirmados o anulados para la empresa.", "NO_CONTENT")
            );
        }

        List<PortfolioWriteOffResponse> responseList = domainList.stream()
                .map(this::buildEnrichedResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }


    private PortfolioWriteOffResponse buildEnrichedResponse(PortfolioWriteOff writeOff) {
        List<Long> invoiceIds = writeOff.getDetails().stream()
                .map(WriteOffDetail::getInvoiceId)
                .toList();

        if (invoiceIds.isEmpty()) {
            return portfolioWriteOffRestMapper.toResponse(writeOff);
        }

        Map<Long, InvoiceReplica> invoiceMap = invoiceProviderPort.findInvoicesByIds(invoiceIds).stream()
                .collect(Collectors.toMap(InvoiceReplica::getId, Function.identity()));

        List<WriteOffDetailResponse> detailResponses = writeOff.getDetails().stream()
                .map(detail -> {
                    InvoiceReplica invoice = invoiceMap.get(detail.getInvoiceId());

                    if (invoice == null) {
                        return null;
                    }

                    InvoiceSummaryResponse invoiceSummary = InvoiceSummaryResponse.builder()
                            .id(invoice.getId())
                            .factCode(invoice.getFactCode())
                            .totalValue(invoice.getTotalValue())
                            .pendingValue(detail.getAmountWrittenOff())
                            .expirationDate(invoice.getExpirationDate())
                            .accountingAccount(invoice.getAccountingAccount())
                            .build();

                    return WriteOffDetailResponse.builder()
                            .amountWrittenOff(detail.getAmountWrittenOff())
                            .invoice(invoiceSummary)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PortfolioWriteOffResponse.builder()
                .id(writeOff.getId())
                .code(writeOff.getCode())
                .justification(writeOff.getJustification())
                .totalAmount(writeOff.getTotalAmount())
                .writeOffDate(writeOff.getWriteOffDate())
                .debitAuxiliaryAccount(writeOff.getDebitAuxiliaryAccount())
                .debitAuxiliaryAccountId(writeOff.getDebitAuxiliaryAccountId())
                .thirdId(writeOff.getThirdId())
                .costCenterId(writeOff.getCostCenterId())
                .status(writeOff.getStatus())
                .enterpriseId(writeOff.getEnterpriseId())
                .details(detailResponses)
                .build();
    }
}
