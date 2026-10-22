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
 * una. En el monolito, person_type y location son datos propios de la
 * empresa que de todas formas se recrean desde cero (no hay un id viejo que
 * conservar), y los catálogos (país/departamento/ciudad, responsabilidad
 * tributaria, tipo de contribuyente, tipo de empresa) no se reinsertan: se
 * reutilizan por id tal cual vienen en el backup. Por eso el único mapeo que
 * efectivamente hace falta aquí es el de la propia empresa (id de origen -&gt;
 * id del restore), pero se deja esta clase genérica por tabla para que, si en
 * el futuro el backup se extiende con datos que sí necesiten remapeo, no haga
 * falta reintroducir una estructura nueva.
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
