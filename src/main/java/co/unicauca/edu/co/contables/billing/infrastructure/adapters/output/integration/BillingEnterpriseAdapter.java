package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Empresa emisora resuelta <strong>en el mismo proceso</strong> mediante
 * {@link IEnterpriseService#findById(UUID)} (no existe puerto de entrada de busqueda; el servicio
 * de dominio devuelve {@code null} si la empresa no existe). Debe ejecutarse dentro de la
 * transaccion de billing porque el mapeo del modulo enterprise toca colecciones perezosas.
 * Un identificador nulo, en blanco o que no sea UUID equivale a empresa inexistente.
 */
@Component("billingEnterpriseAdapter")
public class BillingEnterpriseAdapter implements EnterprisePort {

    private final IEnterpriseService enterpriseService;

    public BillingEnterpriseAdapter(IEnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    @Override
    public Optional<IssuerSnapshot> findIssuer(String enterpriseId) {
        if (enterpriseId == null || enterpriseId.isBlank()) {
            return Optional.empty();
        }
        UUID id;
        try {
            id = UUID.fromString(enterpriseId.trim());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        Enterprise enterprise = enterpriseService.findById(id);
        if (enterprise == null) {
            return Optional.empty();
        }
        return Optional.of(new IssuerSnapshot(
                enterpriseId,
                enterprise.getName(),
                enterprise.getNit(),
                enterprise.getDV(),
                enterprise.getState() == StateEnum.ACTIVE));
    }
}
