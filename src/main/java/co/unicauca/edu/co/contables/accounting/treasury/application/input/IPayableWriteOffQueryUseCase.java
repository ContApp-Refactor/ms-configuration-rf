package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import java.util.List;

public interface IPayableWriteOffQueryUseCase {
    PayableWriteOff find(Long id);
    List<PayableWriteOff> list(String enterpriseId);
}
