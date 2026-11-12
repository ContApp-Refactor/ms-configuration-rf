package co.unicauca.edu.co.contables.commons.backup;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @brief Contrato que implementa cada módulo de negocio para entrar en la copia
 * de seguridad completa de una empresa (ZIP v2).
 *
 * Cada implementación vive en la capa de infraestructura de SU módulo, usa
 * solo SUS repositorios y nunca importa clases de otro módulo: las FKs
 * cruzadas se resuelven con {@link IdMap} y los namespaces de
 * {@link BackupKeys}. El coordinador de empresa recibe
 * {@code List<BackupParticipant>} por inyección y los ejecuta por
 * {@link #order()}.
 *
 * El restore NO debe pasar por servicios de aplicación (publican eventos a
 * Rabbit y disparan auditoría): escribe directo por repositorios JPA.
 */
public interface BackupParticipant {

    /** Clave estable del módulo en el manifest (p. ej. {@code "taxes"}). Ver {@link BackupKeys}. */
    String moduleKey();

    /** Orden de ejecución por dependencias de FKs: menor = antes. */
    int order();

    /** Versión del formato de SUS entradas; el restore rechaza un ZIP con schema mayor. */
    int schemaVersion();

    /** {@code moduleKey}s de los que depende; el coordinador valida el grafo al arrancar. */
    Set<String> requires();

    /**
     * Escribe sus entradas en el ZIP. Solo lee datos de {@code enterpriseId}, que
     * el coordinador ya validó como del tenant actual. Debe leer por páginas o
     * por {@code Stream}, nunca {@code findAll()} completo.
     */
    void export(UUID enterpriseId, BackupWriter writer);

    /**
     * Validaciones previas al restore que NO escriben en la BD (p. ej. el NIT
     * repetido del participante enterprise). El coordinador las llama ANTES de
     * abrir la transacción, con {@code ctx.targetEnterpriseId() == null}, para
     * que el error llegue al usuario como 4xx sin crear un proceso fallido.
     * Por defecto no valida nada.
     */
    default void preflight(BackupReader reader, RestoreContext ctx) {
    }

    /**
     * Lee sus entradas, inserta con ids NUEVOS y registra viejo→nuevo en
     * {@code idMap}. Corre dentro de la transacción única del restore: cualquier
     * excepción provoca rollback total.
     */
    void restore(BackupReader reader, IdMap idMap, RestoreContext ctx);

    /** Conteo en BD para la empresa destino (verificación final). Clave = path de la entrada. */
    Map<String, Long> count(UUID enterpriseId);

    /** Huérfanos: descripciones de FKs que no apuntan a filas de la empresa destino. Vacía = OK. */
    List<String> verifyIntegrity(UUID enterpriseId);

    /**
     * Borra TODOS los datos que este módulo guarda de {@code enterpriseId}, para el
     * restore sobre una empresa existente (modo IN_PLACE). Corre dentro de la
     * transacción única del restore, el coordinador la invoca en orden INVERSO
     * de {@link #order()} (primero los que dependen de otros) y el módulo
     * {@code enterprise} NO borra el registro de la empresa: el restore lo
     * actualiza. Debe borrar solo por repositorios/JPQL del propio módulo, sin
     * publicar eventos ni auditoría, y dejar la BD sin filas de la empresa en
     * sus tablas (hijos antes que padres). Por defecto el módulo no lo soporta
     * y el coordinador rechaza el restore en el lugar con un error claro.
     */
    default void purge(UUID enterpriseId) {
        throw new UnsupportedOperationException(
                "El módulo '" + moduleKey() + "' no soporta restaurar sobre una empresa existente");
    }
}
