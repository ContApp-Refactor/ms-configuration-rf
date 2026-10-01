package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import java.util.List;
import java.util.Optional;

public interface IPayableWriteOffPersistencePort {
    PayableWriteOff save(PayableWriteOff writeOff);
    Optional<PayableWriteOff> find(Long id);
    List<PayableWriteOff> findByEnterprise(String enterpriseId);
}
