package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import java.util.HashMap;
import java.util.Map;

/**
 * @brief Mapa de equivalencias idViejo -&gt; idNuevo por tabla/entidad,
 * usado durante un restore.
 *
 * En el microservicio de referencia este mapeo es imprescindible porque el
 * restore reinserta por JDBC crudo TODAS las tablas de la cadena
 * (person_type, location, enterprise, ...), generando un id nuevo en cada
 * una. En el monolito, los catálogos (país/departamento/ciudad,
 * responsabilidad tributaria, tipo de contribuyente, tipo de empresa) no se
 * reinsertan: se reutilizan por id tal cual vienen en el backup, así que no
 * necesitan remapeo. El id de la empresa tampoco aplica aquí porque es un
 * {@code UUID}, no un {@code Long}.
 * Donde sí hace falta un registro real es en el restore {@code inplace}:
 * {@code location} y {@code person_type} son filas propias de la empresa que
 * se recrean (fila nueva) y cuya fila vieja se da de baja luego, así que el
 * servicio de restore deja aquí la equivalencia id-viejo -&gt; id-nuevo de esas
 * dos tablas a efectos de trazabilidad/depuración.
 */
public class RestoreEquivalenceMap {

    private final Map<String, Map<Long, Long>> equivalencesByTable = new HashMap<>();

    public void put(String table, Long oldId, Long newId) {
        equivalencesByTable.computeIfAbsent(table, t -> new HashMap<>()).put(oldId, newId);
    }

    public Long get(String table, Long oldId) {
        Map<Long, Long> equivalences = equivalencesByTable.get(table);
        return equivalences == null ? null : equivalences.get(oldId);
    }
}
