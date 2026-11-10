package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.billing.domain.model.TaxComponent;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * IVA de referencia de un producto, resuelto <strong>en el mismo proceso</strong> con puertos de
 * entrada: {@link ICategoryServicePort} (ids de impuestos de la categoria) e
 * {@link ITaxSearchInputPort} (impuestos activos de la empresa, campo {@code interest} = porcentaje).
 * El maestro de impuestos no distingue tipos: el porcentaje de la linea es la SUMA de las tarifas de
 * todos los impuestos activos de la categoria (p. ej. 6 + 8 + 12 = 26). Sin categoria, sin impuestos
 * activos, con total invalido (fuera de 0..100) o ante cualquier fallo de la consulta se usa
 * {@code billing.default-vat-rate}: el catalogo nunca bloquea la facturacion.
 *
 * <p>Ademas de la suma devuelve el DESGLOSE ({@link Resolution#taxes()}): un {@link TaxComponent}
 * por impuesto sumado, con el codigo del maestro como nombre ("Iva", "Ica") y su tarifa, en el orden
 * del maestro de impuestos activos. Es solo presentacion: el calculo sigue usando la suma. Cuando se
 * usa el valor por defecto el desglose queda vacio (la linea muestra un unico valor editable).
 *
 * <p>Los impuestos activos se consultan una sola vez por invocacion de {@link #resolveAll} (H7) y
 * nunca se cachean entre invocaciones.
 *
 * <p><strong>Limitacion conocida (H8):</strong> el {@code catch (RuntimeException)} garantiza que ESTE
 * componente devuelva el valor por defecto, pero no repara la transaccion: si la consulta falla en un
 * repositorio de Spring Data que participa en la transaccion de emitir/guardar, Spring ya la marco
 * <em>rollback-only</em> y la emision terminaria en {@code UnexpectedRollbackException}. Se evaluo
 * aislar las consultas con un colaborador {@code REQUIRES_NEW}/{@code NOT_SUPPORTED} y NO se
 * implemento: abre una segunda conexion mientras la transaccion de emitir conserva la suya (riesgo de
 * agotar el pool bajo emisiones concurrentes) y complica el aislamiento por tenant. La mitigacion
 * actual es reducir las consultas (H7); la solucion de fondo es resolver el IVA antes de abrir la
 * transaccion de emitir.
 */
@Component("billingVatRateResolver")
public class BillingVatRateResolver {

    private static final Logger log = LoggerFactory.getLogger(BillingVatRateResolver.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ICategoryServicePort categoryService;
    private final ITaxSearchInputPort taxSearch;
    private final BillingProperties properties;

    public BillingVatRateResolver(ICategoryServicePort categoryService, ITaxSearchInputPort taxSearch,
            BillingProperties properties) {
        this.categoryService = categoryService;
        this.taxSearch = taxSearch;
        this.properties = properties;
    }

    /**
     * Resultado para una categoria: {@code rate} es la suma (0..100) que usa el calculo y
     * {@code taxes} el desglose que la explica; vacio cuando {@code rate} es el valor por defecto.
     */
    public record Resolution(BigDecimal rate, List<TaxComponent> taxes) {
        public Resolution {
            taxes = TaxComponent.copyOf(taxes);
        }
    }

    /** Porcentaje de IVA (0..100, escala 2) para la categoria; nunca nulo. */
    public BigDecimal resolve(String enterpriseId, Long categoryId) {
        return resolveBreakdown(enterpriseId, categoryId).rate();
    }

    /** Suma y desglose de la categoria; nunca nulo. */
    public Resolution resolveBreakdown(String enterpriseId, Long categoryId) {
        return resolveAllBreakdowns(enterpriseId, Collections.singletonList(categoryId)).get(categoryId);
    }

    /**
     * IVA de varias categorias en una sola operacion (H7): los impuestos activos de la empresa se
     * consultan UNA vez (y solo si alguna categoria tiene impuestos) y cada categoria distinta una
     * vez. Nada se conserva al terminar: los impuestos pueden cambiar entre peticiones. La clave
     * {@code null} (producto sin categoria) se admite y devuelve el valor por defecto.
     */
    public Map<Long, BigDecimal> resolveAll(String enterpriseId, Collection<Long> categoryIds) {
        Map<Long, BigDecimal> rates = new LinkedHashMap<>();
        resolveAllBreakdowns(enterpriseId, categoryIds).forEach((id, resolution) -> rates.put(id, resolution.rate()));
        return rates;
    }

    /** Como {@link #resolveAll} pero con el desglose de cada categoria (mismas consultas, H7). */
    public Map<Long, Resolution> resolveAllBreakdowns(String enterpriseId, Collection<Long> categoryIds) {
        Map<Long, Resolution> resolutions = new LinkedHashMap<>();
        Operation operation = new Operation(enterpriseId);
        for (Long categoryId : categoryIds) {
            if (!resolutions.containsKey(categoryId)) {
                resolutions.put(categoryId, resolveOne(operation, categoryId));
            }
        }
        return resolutions;
    }

    private Resolution resolveOne(Operation operation, Long categoryId) {
        if (categoryId == null) {
            return fallback();
        }
        try {
            Resolution resolution = fromCategory(operation, categoryId);
            return resolution == null ? fallback() : resolution;
        } catch (RuntimeException ex) {
            log.warn("No se pudo resolver el IVA de la categoria {} (empresa {}); se usa el valor por defecto: {}",
                    categoryId, operation.enterpriseId, ex.toString());
            return fallback();
        }
    }

    private Resolution fromCategory(Operation operation, Long categoryId) {
        Category category = categoryService.findById(operation.enterpriseId, categoryId);
        if (category == null || category.getTaxes() == null || category.getTaxes().isEmpty()) {
            return null;
        }
        List<Long> categoryTaxIds = category.getTaxes();
        List<Tax> active = operation.activeTaxes();
        if (active == null) {
            return null;
        }
        // Todos los impuestos activos de la categoria se suman (el maestro no distingue tipos).
        List<Tax> candidates = active.stream()
                .filter(t -> categoryTaxIds.contains(t.getId()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        BigDecimal rate = candidates.stream().map(BillingVatRateResolver::rateOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        if (rate.signum() < 0 || rate.compareTo(HUNDRED) > 0) {
            return null;
        }
        return new Resolution(rate, candidates.stream()
                .map(t -> TaxComponent.of(nameOf(t), rateOf(t)))
                .toList());
    }

    /** Nombre tal como esta en el maestro: el codigo ("Iva"); sin codigo, la descripcion. */
    private static String nameOf(Tax tax) {
        if (tax.getCode() != null && !tax.getCode().isBlank()) {
            return tax.getCode();
        }
        if (tax.getDescription() != null && !tax.getDescription().isBlank()) {
            return tax.getDescription();
        }
        return "Impuesto";
    }

    private static BigDecimal rateOf(Tax tax) {
        return tax.getInterest() == null ? BigDecimal.ZERO : BigDecimal.valueOf(tax.getInterest());
    }

    /** Estado de UNA invocacion: carga perezosa, una sola vez, de los impuestos activos de la empresa. */
    private final class Operation {
        private final String enterpriseId;
        private List<Tax> taxes;
        private boolean loaded;

        private Operation(String enterpriseId) {
            this.enterpriseId = enterpriseId;
        }

        private List<Tax> activeTaxes() {
            if (!loaded) {
                loaded = true; // un fallo no se reintenta en la misma operacion: todas las categorias usan el respaldo
                taxes = taxSearch.getActiveTaxes(enterpriseId);
            }
            return taxes;
        }
    }

    private Resolution fallback() {
        return new Resolution(properties.getDefaultVatRate(), List.of());
    }
}
