package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
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

    /** Longitud maxima de la referencia del logo que se congela en el documento. */
    static final int MAX_LOGO_REFERENCE = 500;

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
        Location location = enterprise.getLocation();
        return Optional.of(new IssuerSnapshot(
                enterpriseId,
                enterprise.getName(),
                enterprise.getNit(),
                enterprise.getDV(),
                enterprise.getState() == StateEnum.ACTIVE,
                location == null ? null : blankToNull(location.getAddress(), 300),
                location == null || location.getCity() == null ? null : blankToNull(location.getCity().getName(), 150),
                location == null || location.getDepartment() == null ? null
                        : blankToNull(location.getDepartment().getName(), 150),
                blankToNull(enterprise.getPhone(), 30),
                blankToNull(enterprise.getEmail(), 150),
                enterprise.getMainActivity() == null ? null : String.valueOf(enterprise.getMainActivity()),
                lightLogo(enterprise.getLogo())));
    }

    /**
     * El logo se congela solo si es una referencia liviana (URL o ruta, p. ej. {@code /api/enterprises/logos/<key>}).
     * Un contenido embebido ({@code data:}) o demasiado largo no se copia al documento: queda nulo y el
     * cliente lo pide a Empresa.
     */
    private static String lightLogo(String logo) {
        String value = blankToNull(logo, Integer.MAX_VALUE);
        if (value == null || value.regionMatches(true, 0, "data:", 0, 5) || value.length() > MAX_LOGO_REFERENCE) {
            return null;
        }
        return value;
    }

    /** Recortado y limitado al ancho de la columna; en blanco queda nulo. */
    private static String blankToNull(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
