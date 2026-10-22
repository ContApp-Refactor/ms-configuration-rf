package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentSchedule;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AccountingResult;
import java.time.LocalDate;
import java.time.Instant;

public interface IPaymentScheduleExecutionUseCase {
    PaymentSchedule execute(Long id);
    void executeDue(LocalDate date);
    void applyAccountingResult(AccountingResult result);
    void recoverAbandoned(Instant before);
}
