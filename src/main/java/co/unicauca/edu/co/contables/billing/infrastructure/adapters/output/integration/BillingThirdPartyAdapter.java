package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.application.ports.output.ThirdPartyPort;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.GetThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Terceros del modulo thirds, resueltos <strong>en el mismo proceso</strong> mediante su puerto de
 * entrada {@link GetThirdUseCase} (nunca repositorios ni entidades). Solo se valida existencia y
 * estado (H0-3): no se filtra por {@code ThirdType}; el rol solicitado se refleja en el snapshot.
 * Un tercero inexistente ({@link ThirdNotFound}) se traduce a vacio; cualquier otro error se propaga.
 */
@Component("billingThirdPartyAdapter")
public class BillingThirdPartyAdapter implements ThirdPartyPort {

    private final GetThirdUseCase getThirdUseCase;

    public BillingThirdPartyAdapter(GetThirdUseCase getThirdUseCase) {
        this.getThirdUseCase = getThirdUseCase;
    }

    @Override
    public Optional<CounterpartySnapshot> findCounterparty(String enterpriseId, long thirdId, CounterpartyRole role) {
        Third third;
        try {
            third = getThirdUseCase.getThirdById(thirdId, enterpriseId);
        } catch (ThirdNotFound ex) {
            return Optional.empty();
        }
        if (third == null) {
            return Optional.empty();
        }
        return Optional.of(new CounterpartySnapshot(
                thirdId,
                role,
                displayName(third),
                third.getTypeId() == null ? null : third.getTypeId().getTypeId(),
                third.getIdNumber() == null ? null : String.valueOf(third.getIdNumber()),
                third.getVerificationNumber() == null ? null : String.valueOf(third.getVerificationNumber()),
                third.getAddress(),
                third.getEmail(),
                third.getPhoneNumber(),
                third.isActive()));
    }

    /** Razon social si existe; si no, nombres y apellidos concatenados y recortados. */
    private static String displayName(Third third) {
        String socialReason = third.getSocialReason();
        if (socialReason != null && !socialReason.isBlank()) {
            return socialReason.trim();
        }
        String names = third.getNames() == null ? "" : third.getNames();
        String lastNames = third.getLastNames() == null ? "" : third.getLastNames();
        return (names + " " + lastNames).trim();
    }
}
