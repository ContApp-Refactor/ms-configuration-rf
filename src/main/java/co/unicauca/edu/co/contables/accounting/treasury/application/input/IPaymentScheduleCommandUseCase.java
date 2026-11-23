package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentSchedule;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.Schedule;

public interface IPaymentScheduleCommandUseCase {
    PaymentSchedule create(Schedule command);
    PaymentSchedule update(Long id, Schedule command);
    void delete(Long id);
    PaymentSchedule cancel(Long id);
}
