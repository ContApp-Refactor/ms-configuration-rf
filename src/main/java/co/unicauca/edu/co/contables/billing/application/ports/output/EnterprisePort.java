package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import java.util.Optional;

/** Acceso al modulo de empresas (emisor del documento). */
public interface EnterprisePort {

    /** Snapshot del emisor, o vacio si la empresa no existe o el identificador es invalido. */
    Optional<IssuerSnapshot> findIssuer(String enterpriseId);
}
