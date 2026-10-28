package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application.AccountingNoteService;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AnnulAccountingNoteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/accountCatalogue/accounting-notes")
public class AccountingNoteController {

    private static final Logger log = LoggerFactory.getLogger(AccountingNoteController.class);

    private final AccountingNoteService accountingNoteService;
    private final co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application.MovementQueryService movementQueryService;

    public AccountingNoteController(AccountingNoteService accountingNoteService,
                                    co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application.MovementQueryService movementQueryService) {
        this.accountingNoteService = accountingNoteService;
        this.movementQueryService = movementQueryService;
    }


    @PostMapping
    public ResponseEntity<AccountingNoteService.AccountingNoteResponse> create(
            @Valid @RequestBody AccountingNoteRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(accountingNoteService.create(request));
        } catch (RuntimeException ex) {
            log.error("Error al crear nota contable; originModule={}", request.originModule(), ex);
            throw ex;
        }
    }

    @PutMapping("/{id}")
    public AccountingNoteService.AccountingNoteResponse updateDraft(
            @PathVariable Long id, @Valid @RequestBody AccountingNoteRequest request) {
        return accountingNoteService.updateDraft(id, request);
    }

    @PostMapping("/{id}/post")
    public AccountingNoteService.AccountingNoteResponse postDraft(@PathVariable Long id,
            @RequestParam String enterpriseId) {
        return accountingNoteService.postDraft(id, enterpriseId);
    }

    @PostMapping("/{id}/annul")
    public AccountingNoteService.AccountingNoteResponse annul(@PathVariable Long id,
            @Valid @RequestBody AnnulAccountingNoteRequest request) {
        return accountingNoteService.annul(id, request.enterpriseId(), request.originModule(), request.reason());
    }

    @GetMapping("/{id}")
    public AccountingNoteService.AccountingNoteResponse findById(@PathVariable Long id,
            @RequestParam String enterpriseId) {
        return accountingNoteService.findById(id, enterpriseId);
    }

    @GetMapping
    public List<AccountingNoteService.AccountingNoteResponse> findAll(@RequestParam String enterpriseId) {
        return accountingNoteService.findAll(enterpriseId);
    }

    @GetMapping("/movements")
    public co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application.MovementQueryService.MovementResponse movements(
            @RequestParam String accountCode,
            @RequestParam String enterpriseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String thirdPartyId,
            @RequestParam(required = false) String costCenterCode,
            @RequestParam(required = false) String originModule) {
        return movementQueryService.query(accountCode, enterpriseId, startDate, endDate,
                thirdPartyId, costCenterCode, originModule);
    }
}
