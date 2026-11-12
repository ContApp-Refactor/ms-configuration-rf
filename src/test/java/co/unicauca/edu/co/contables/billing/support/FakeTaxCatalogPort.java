package co.unicauca.edu.co.contables.billing.support;

import co.unicauca.edu.co.contables.billing.application.ports.output.TaxCatalogPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catalogo de impuestos en memoria: solo los ACTIVOS se listan, igual que el adaptador real. */
public class FakeTaxCatalogPort implements TaxCatalogPort {

    private final Map<Long, TaxRef> active = new LinkedHashMap<>();
    private int listCalls;

    /** Impuestos del ejemplo de retenciones: 1 = a (5%), 2 = b (10%), 3 = c (20%). */
    public static FakeTaxCatalogPort withExampleTaxes() {
        return new FakeTaxCatalogPort().withTax(1L, "a", "5").withTax(2L, "b", "10").withTax(3L, "c", "20");
    }

    public FakeTaxCatalogPort withTax(long id, String name, String ratePct) {
        active.put(id, new TaxRef(id, name, new BigDecimal(ratePct)));
        return this;
    }

    /** Desactiva (o borra) el impuesto: deja de aparecer en el catalogo. */
    public FakeTaxCatalogPort deactivate(long id) {
        active.remove(id);
        return this;
    }

    public int listCalls() {
        return listCalls;
    }

    @Override
    public List<TaxRef> listActiveTaxes(String enterpriseId) {
        listCalls++;
        return new ArrayList<>(active.values());
    }
}
