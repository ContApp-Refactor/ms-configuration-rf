package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.adapter;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.ThirdPartyPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import org.springframework.stereotype.Component;

/** Adapter local de solo lectura al catálogo de terceros. */
@Component
public class ThirdPartyAdapter implements ThirdPartyPort {

    private final ThirdOutputPort thirds;

    public ThirdPartyAdapter(ThirdOutputPort thirds) {
        this.thirds = thirds;
    }

    @Override
    public boolean existsAndIsActive(String documentNumber, String enterpriseId) {
        final long idNumber;
        try {
            idNumber = Long.parseLong(documentNumber);
        } catch (NumberFormatException ex) {
            return false;
        }
        return thirds.getThirdById(idNumber, enterpriseId)
                .map(third -> Boolean.TRUE.equals(third.getState()))
                .orElse(false);
    }
}
