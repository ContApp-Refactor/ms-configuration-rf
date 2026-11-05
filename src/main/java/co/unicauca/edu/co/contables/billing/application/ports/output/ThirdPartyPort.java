package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import java.util.Optional;

/** Acceso al modulo de terceros (cliente o proveedor). */
public interface ThirdPartyPort {

    /** Snapshot del tercero con el rol pedido, o vacio si no existe en la empresa. */
    Optional<CounterpartySnapshot> findCounterparty(String enterpriseId, long thirdId, CounterpartyRole role);
}
