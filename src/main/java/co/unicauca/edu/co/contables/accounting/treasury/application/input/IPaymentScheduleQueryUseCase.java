package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentSchedule;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.ScheduleFilter;
import java.util.List;

public interface IPaymentScheduleQueryUseCase {
    PaymentSchedule find(Long id);
    List<PaymentSchedule> list(ScheduleFilter filter);
}
