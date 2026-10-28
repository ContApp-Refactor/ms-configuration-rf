package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.repository.AccountingNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MovementQueryService {
    private final AccountingNoteRepository repository;

    public MovementQueryService(AccountingNoteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public MovementResponse query(String accountCode, String enterpriseId, LocalDate startDate, LocalDate endDate,
                                  String thirdPartyId, String costCenterCode, String originModule) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        BigDecimal previousBalance = repository.findPreviousBalance(accountCode, enterpriseId, startDate,
                thirdPartyId, costCenterCode, originModule);
        List<MovementResponse.Item> movements = repository.findMovements(accountCode, enterpriseId,
                        startDate, endDate, thirdPartyId, costCenterCode, originModule)
                .stream().map(item -> new MovementResponse.Item(
                        item.getNoteId(), item.getNoteNumber(), item.getMovementDate(), item.getDocumentTypeCode(),
                        item.getConcept(), item.getOriginModule(), item.getOriginId(), item.getStatus(),
                        item.getAccountCode(), item.getThirdPartyId(), item.getCostCenterCode(),
                        item.getDescription(), item.getDebit(), item.getCredit())).toList();
        BigDecimal totalDebit = movements.stream().filter(MovementQueryService::isIncludedInBalance)
                .map(MovementResponse.Item::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = movements.stream().filter(MovementQueryService::isIncludedInBalance)
                .map(MovementResponse.Item::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MovementResponse(previousBalance, movements, totalDebit, totalCredit,
                previousBalance.add(totalDebit).subtract(totalCredit));
    }

    private static boolean isIncludedInBalance(MovementResponse.Item item) {
        return "POSTED".equals(item.status()) || "ACTIVE".equals(item.status());
    }

    public record MovementResponse(BigDecimal previousBalance, List<Item> movements,
                                   BigDecimal totalDebit, BigDecimal totalCredit, BigDecimal closingBalance) {
        public record Item(Long noteId, String noteNumber, LocalDate date, String documentTypeCode,
                           String concept, String originModule, String originId, String status,
                           String accountCode, String thirdPartyId, String costCenterCode,
                           String description, BigDecimal debit, BigDecimal credit) { }
    }
}
