package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resuelve la cuenta CxP para facturas de compra consultando el catálogo real.
 * Solo acepta cuentas con semántica de obligaciones con proveedores (CxP / proveedores).
 *
 * El catálogo se consulta inyectando el puerto de entrada del módulo catálogo;
 * no hay llamadas HTTP internas.
 */
@Component
@Slf4j
public class PayableAccountDefaultResolver {

    static final String NO_VALID_CATALOGUE_MESSAGE =
            "La empresa aún no tiene un catálogo de cuentas válido. Configure el catálogo de cuentas "
                    + "en Maestros Generales antes de registrar facturas de compra.";
    static final String NO_ACTIVE_PAYABLE_MESSAGE =
            "La empresa no tiene configurada una Cuenta por Pagar activa en el catálogo de cuentas.";
    static final String ENTERPRISE_REQUIRED_MESSAGE =
            "El ID de la empresa es requerido para resolver la cuenta por pagar.";
    static final String NULL_CATALOGUE_RESULT_MESSAGE =
            "El puerto del catálogo de cuentas devolvió null";

    private final IAccountCatalogueSearchInputPort catalogueSearchInputPort;

    public PayableAccountDefaultResolver(IAccountCatalogueSearchInputPort catalogueSearchInputPort) {
        this.catalogueSearchInputPort = catalogueSearchInputPort;
    }

    /**
     * @param requestedAccountId cuenta explícita (API legacy); null para default automático
     */
    public Long resolveForPurchase(Long requestedAccountId, String enterpriseId) {
        if (enterpriseId == null || enterpriseId.isBlank()) {
            throw new IllegalArgumentException(ENTERPRISE_REQUIRED_MESSAGE);
        }
        List<CatalogueAccount> accounts = loadAccounts(enterpriseId);
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException(NO_VALID_CATALOGUE_MESSAGE);
        }
        if (requestedAccountId != null) {
            CatalogueAccount explicit = accounts.stream()
                    .filter(account -> requestedAccountId.equals(account.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La cuenta contable " + requestedAccountId + " no existe en el catálogo de la empresa"));
            if (!isActive(explicit)) {
                throw new IllegalArgumentException(
                        "La cuenta CxP " + requestedAccountId + " está inactiva y no puede usarse en facturas nuevas");
            }
            if (!isPayableAccount(explicit)) {
                throw new IllegalArgumentException(
                        "La cuenta " + requestedAccountId + " no es una cuenta por pagar válida");
            }
            return explicit.id();
        }
        return selectDefaultPayable(accounts)
                .map(CatalogueAccount::id)
                .orElseThrow(() -> new IllegalArgumentException(NO_ACTIVE_PAYABLE_MESSAGE));
    }

    private Optional<CatalogueAccount> selectDefaultPayable(List<CatalogueAccount> accounts) {
        return accounts.stream()
                .filter(this::isPayableAccount)
                .sorted(Comparator
                        .comparingInt(this::payableSelectionPriority)
                        .thenComparing(account -> account.code() == null ? "" : account.code()))
                .findFirst();
    }

    /**
     * Cuenta elegible solo si representa CxP/proveedores según nombre/descripción del catálogo.
     * No basta con ser pasivo ni con comenzar por 22.
     */
    boolean isPayableAccount(CatalogueAccount account) {
        if (!isActive(account)) {
            return false;
        }
        String code = normalized(account.code());
        String description = normalized(account.description());
        if (isExcludedFromSupplierPayables(code, description)) {
            return false;
        }
        return matchesSupplierPayableSemantics(description);
    }

    private boolean isExcludedFromSupplierPayables(String code, String description) {
        if (code.startsWith("11")) {
            return true;
        }
        if (description.contains("caja")
                || description.contains("banco")
                || description.contains("efectivo")
                || description.contains("reserva")) {
            return true;
        }
        return description.contains("beneficios a empleados")
                || description.contains("beneficio a empleado")
                || description.contains("obligaciones laborales")
                || description.contains("obligacion laboral")
                || description.contains("obligaciones financieras")
                || description.contains("obligacion financiera")
                || description.contains("impuestos por pagar")
                || description.contains("impuesto por pagar")
                || description.contains("salarios por pagar")
                || description.contains("salario por pagar")
                || description.contains("retenciones por pagar")
                || description.contains("retencion por pagar")
                || description.contains("nomina por pagar")
                || description.contains("nómina por pagar");
    }

    private boolean matchesSupplierPayableSemantics(String description) {
        return description.contains("cuentas por pagar")
                || description.contains("cuenta por pagar")
                || description.contains("proveedores")
                || description.contains("proveedor nacional")
                || description.contains("proveedor extranjero")
                || description.contains("proveedor ");
    }

    private int payableSelectionPriority(CatalogueAccount account) {
        String description = normalized(account.description());
        if (description.contains("cuenta por pagar") || description.contains("cuentas por pagar")) {
            return 0;
        }
        if (description.contains("proveedor")) {
            return 1;
        }
        return 2;
    }

    private static String normalized(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private boolean isActive(CatalogueAccount account) {
        return account.status() == null || Boolean.TRUE.equals(account.status());
    }

    private List<CatalogueAccount> loadAccounts(String enterpriseId) {
        List<AccountCatalogue> accounts = queryCatalogue(enterpriseId);
        if (accounts == null) {
            log.error("El puerto del catálogo devolvió null para la empresa {}", enterpriseId);
            throw new IllegalStateException(NULL_CATALOGUE_RESULT_MESSAGE + " para la empresa " + enterpriseId);
        }
        List<CatalogueAccount> catalogue = new ArrayList<>();
        for (AccountCatalogue account : accounts) {
            catalogue.add(new CatalogueAccount(
                    account.getId(),
                    account.getCode(),
                    account.getDescription(),
                    account.getClassification() != null ? account.getClassification().getState() : null,
                    account.getStatus()));
        }
        return catalogue;
    }

    /**
     * Consulta el catálogo aislando los fallos del puerto. Un {@code null} de
     * retorno no se trata aquí: viola el contrato del puerto y lo decide
     * {@link #loadAccounts}, para no confundirlo con "catálogo sin configurar".
     */
    private List<AccountCatalogue> queryCatalogue(String enterpriseId) {
        try {
            return catalogueSearchInputPort.getAllAccountsByEnterprise(enterpriseId);
        } catch (Exception ex) {
            log.warn("No se pudo consultar catálogo para empresa {}: {}", enterpriseId, ex.getMessage());
            throw new IllegalStateException("No se pudo consultar el catálogo de cuentas para resolver CxP", ex);
        }
    }

    record CatalogueAccount(Long id, String code, String description, String classification, Boolean status) {}
}
