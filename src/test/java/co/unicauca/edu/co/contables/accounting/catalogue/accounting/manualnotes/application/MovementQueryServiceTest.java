package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.repository.AccountingNoteRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.repository.AccountingNoteRepository.MovementProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovementQueryServiceTest {
    private AccountingNoteRepository repository;
    private MovementQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(AccountingNoteRepository.class);
        service = new MovementQueryService(repository);
    }

    @Test
    void returnsConsolidatedMovementsAndBalancesOnlyPostedRows() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        when(repository.findPreviousBalance("11050501", "enterprise-1", from, "9001", "CC01", null))
                .thenReturn(new BigDecimal("25.00"));
        when(repository.findMovements("11050501", "enterprise-1", from, to, "9001", "CC01", null))
                .thenReturn(List.of(
                        row("POSTED", "100.00", "0.00", "MANUAL"),
                        row("ACTIVE", "0.00", "40.00", "RECEIPT"),
                        row("VOIDED", "900.00", "0.00", "INVOICE")));

        MovementQueryService.MovementResponse response = service.query(
                "11050501", "enterprise-1", from, to, "9001", "CC01", null);

        assertEquals(3, response.movements().size());
        assertEquals(new BigDecimal("100.00"), response.totalDebit());
        assertEquals(new BigDecimal("40.00"), response.totalCredit());
        assertEquals(new BigDecimal("85.00"), response.closingBalance());
    }

    @Test
    void rejectsAnInvertedDateRangeBeforeQueryingRepository() {
        assertThrows(IllegalArgumentException.class, () -> service.query(
                "11050501", "enterprise-1", LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1), null, null, null));

        verify(repository, never()).findMovements("11050501", "enterprise-1",
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), null, null, null);
    }

    private static MovementProjection row(String status, String debit, String credit, String origin) {
        return new MovementProjection() {
            @Override public Long getNoteId() { return 1L; }
            @Override public String getNoteNumber() { return "NC-0001"; }
            @Override public LocalDate getMovementDate() { return LocalDate.of(2026, 1, 15); }
            @Override public String getDocumentTypeCode() { return "NC"; }
            @Override public String getConcept() { return "Test"; }
            @Override public String getOriginModule() { return origin; }
            @Override public String getOriginId() { return "source-1"; }
            @Override public String getStatus() { return status; }
            @Override public String getAccountCode() { return "11050501"; }
            @Override public String getThirdPartyId() { return "9001"; }
            @Override public String getCostCenterCode() { return "CC01"; }
            @Override public String getDescription() { return "Line"; }
            @Override public BigDecimal getDebit() { return new BigDecimal(debit); }
            @Override public BigDecimal getCredit() { return new BigDecimal(credit); }
        };
    }
}
