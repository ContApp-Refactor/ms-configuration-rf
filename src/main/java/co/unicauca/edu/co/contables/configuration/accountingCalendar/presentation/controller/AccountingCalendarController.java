package co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.controller;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.mapper.AccountingCalendarDomainMapper;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services.IAccountingCalendarService;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request.*;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.response.AccountingCalendarRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @brief Controlador REST para el calendario contable
 *
 * Expone endpoints REST para gestionar operaciones CRUD y por lotes
 * del calendario contable, incluyendo creación, consulta y eliminación.
 */
@RestController
@RequestMapping("/api/config/accounting-calendar")
@RequiredArgsConstructor
public class AccountingCalendarController {

    private final IAccountingCalendarService service;
    private final AccountingCalendarDomainMapper mapper;

    @PreAuthorize("hasAuthority('A_C#OD')")
    @PostMapping("/create")
    public ResponseEntity<AccountingCalendarRes> create(@Valid @RequestBody AccountingCalendarCreateReq req) {
        AccountingCalendar created = service.create(req);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @GetMapping("/findById/{id}/{enterpriseId}")
    public ResponseEntity<AccountingCalendarRes> getById(@PathVariable Long id, @PathVariable String enterpriseId) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id, enterpriseId)));
    }

    @PreAuthorize("hasAuthority('A_C#CD')")
    @DeleteMapping("/delete/{id}/{enterpriseId}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @PathVariable String enterpriseId) {
        service.delete(id, enterpriseId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('A_C#OM')")
    @PostMapping("/open-month")
    public ResponseEntity<List<AccountingCalendarRes>> openMonthBatch(@Valid @RequestBody AccountingCalendarCreateMonthReq req) {
        List<AccountingCalendar> created = service.openMonthBatch(req);
        return ResponseEntity.ok(created.stream().map(mapper::toRes).collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('A_C#CM')")
    @DeleteMapping("/delete-month")
    public ResponseEntity<Void> deleteByMonth(@Valid @RequestBody AccountingCalendarDeleteMonthReq req) {
        service.deleteByMonth(req);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('A_C#OY')")
    @PostMapping("/open-year")
    public ResponseEntity<List<AccountingCalendarRes>> openYearBatch(@Valid @RequestBody AccountingCalendarCreateYearReq req) {
        List<AccountingCalendar> created = service.openYearBatch(req);
        return ResponseEntity.ok(created.stream().map(mapper::toRes).collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('A_C#CY')")
    @DeleteMapping("/delete-year")
    public ResponseEntity<Void> deleteByYear(@Valid @RequestBody AccountingCalendarDeleteYearReq req) {
        service.deleteByYear(req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/year/{enterpriseId}")
    public ResponseEntity<List<AccountingCalendarRes>> findAllByYear(
            @PathVariable String enterpriseId,
            @RequestParam int year) {
        List<AccountingCalendar> calendars = service.findAllByEnterpriseAndYear(enterpriseId, year);
        return ResponseEntity.ok(calendars.stream().map(mapper::toRes).collect(Collectors.toList()));
    }

    @GetMapping("/years/{enterpriseId}")
    public ResponseEntity<List<Integer>> findExistingYears(@PathVariable String enterpriseId) {
        List<Integer> years = service.findExistingYearsByEnterprise(enterpriseId);
        return ResponseEntity.ok(years);
    }

    @GetMapping("/exists/{enterpriseId}")
    public ResponseEntity<Boolean> existsDate(
            @PathVariable String enterpriseId,
            @RequestParam LocalDate date) {
        boolean exists = service.existsDate(enterpriseId, date);
        return ResponseEntity.ok(exists);
    }

}
