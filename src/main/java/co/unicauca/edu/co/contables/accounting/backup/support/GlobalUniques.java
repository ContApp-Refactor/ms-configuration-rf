package co.unicauca.edu.co.contables.accounting.backup.support;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.IdMap;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Números de documento que la BD exige únicos GLOBALMENTE (a través de todos los tenants y
 * empresas), a diferencia del resto de la copia, que es por empresa.
 *
 * Casos reales de este módulo: {@code accounting_entries.code}, {@code receipts.receipt_code},
 * {@code accounting_receipts.original_receipt_id} y {@code FACTURES.fact_code} (UNIQUE a nivel de
 * columna). Además {@code fact_code} se usa como clave de búsqueda sin filtrar por empresa
 * ({@code findByFactCode}), por lo que un número repetido en el mismo tenant rompería las consultas de
 * la empresa ORIGEN al duplicar.
 *
 * Política: se CONSERVA el número original si está libre en toda la BD (restore en un entorno limpio) y
 * solo si ya existe se asigna uno nuevo. Las consultas son nativas a propósito: el filtro {@code @TenantId}
 * de Hibernate ocultaría las filas de otros tenants, que son justo las que chocan.
 */
@Component
@RequiredArgsConstructor
public class GlobalUniques {

    /** Tablas.columna que guardan el número de factura. */
    private static final String[] FACT_CODE_COLUMNS = {
            "FACTURES.fact_code", "skeleton_factures.fact_code",
            "accounting_invoice_replica.fact_code", "invoice_replica.fact_code" };

    private final EntityManager entityManager;

    /** Marca de agua de números asignados en este restore (el IdMap identifica el restore en curso). */
    private final Map<IdMap, long[]> factCodeHighWater = Collections.synchronizedMap(new WeakHashMap<>());

    /** {@code true} si algún registro (de cualquier tenant) tiene ese valor en {@code table.column}. */
    public boolean taken(String table, String column, Object value) {
        TxSupport.flushIfActive(entityManager);
        Number n = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = :v")
                .setParameter("v", value)
                .getSingleResult();
        return n.longValue() > 0;
    }

    /**
     * Devuelve {@code original} si está libre; si no, el primer valor generado por {@code generator} que
     * esté libre en BD y no esté en {@code usedInThisRestore} (donde registra el elegido).
     */
    public String freeCode(String table, String column, String original, Supplier<String> generator,
            Set<String> usedInThisRestore) {
        String candidate = original;
        int guard = 0;
        while (candidate == null || usedInThisRestore.contains(candidate) || taken(table, column, candidate)) {
            candidate = generator.get();
            if (++guard > 1000) {
                throw new IllegalStateException("No se pudo generar un código libre para " + table + "." + column);
            }
        }
        usedInThisRestore.add(candidate);
        return candidate;
    }

    /**
     * Número de factura destino para {@code oldCode}: el mismo si está libre, o el siguiente al máximo
     * global. Idempotente por restore: el primer participante que lo necesita lo decide y lo registra en
     * {@code FACT_CODE}; el resto lo reutiliza, así el mismo número es consistente en todas las tablas.
     */
    public Long factCode(IdMap idMap, Long oldCode) {
        if (oldCode == null) {
            return null;
        }
        Optional<Long> known = idMap.find(AccountingBackupKeys.FACT_CODE, oldCode);
        if (known.isPresent()) {
            return known.get();
        }
        long chosen = oldCode;
        if (anyFactCodeTaken(oldCode)) {
            long[] hwm = factCodeHighWater.computeIfAbsent(idMap, k -> new long[] { 0L });
            chosen = Math.max(maxFactCode(), hwm[0]) + 1;
            hwm[0] = chosen;
        }
        idMap.put(AccountingBackupKeys.FACT_CODE, oldCode, chosen);
        return chosen;
    }

    private boolean anyFactCodeTaken(long code) {
        for (String tc : FACT_CODE_COLUMNS) {
            int dot = tc.indexOf('.');
            if (taken(tc.substring(0, dot), tc.substring(dot + 1), code)) {
                return true;
            }
        }
        return false;
    }

    private long maxFactCode() {
        TxSupport.flushIfActive(entityManager);
        long max = 0;
        for (String tc : FACT_CODE_COLUMNS) {
            int dot = tc.indexOf('.');
            Number n = (Number) entityManager
                    .createNativeQuery("SELECT COALESCE(MAX(" + tc.substring(dot + 1) + "), 0) FROM " + tc.substring(0, dot))
                    .getSingleResult();
            max = Math.max(max, n.longValue());
        }
        return max;
    }
}
