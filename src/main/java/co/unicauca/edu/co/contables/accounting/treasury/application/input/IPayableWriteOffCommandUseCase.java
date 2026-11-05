package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AccountingResult;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.WriteOff;

public interface IPayableWriteOffCommandUseCase {
    PayableWriteOff create(WriteOff command);
    PayableWriteOff confirm(Long id);
    PayableWriteOff discardDraft(Long id);
    PayableWriteOff voidWriteOff(Long id);
    void applyAccountingResult(AccountingResult result);
}
