package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port;

import java.util.Optional;

public interface AccountPort {

    Optional<Account> findByCode(String code, String enterpriseId);

    record Account(
            String code,
            String name,
            boolean auxiliary,
            boolean requiresCostCenter,
            boolean requiresThirdParty) {
    }
}
