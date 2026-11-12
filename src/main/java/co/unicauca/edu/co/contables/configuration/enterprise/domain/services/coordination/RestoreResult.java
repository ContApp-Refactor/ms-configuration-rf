package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @brief Resultado de un restore confirmado.
 *
 * @param enterpriseId     empresa nueva creada
 * @param formatVersion    versión del ZIP (1 o 2)
 * @param restoredModules  módulos restaurados, en orden de ejecución
 * @param skippedModules   módulos del ZIP sin participante que se omitieron ({@code skipUnknownModules})
 * @param counts           conteos verificados en la BD destino (clave = path de la entrada)
 */
public record RestoreResult(UUID enterpriseId, int formatVersion, List<String> restoredModules,
        List<String> skippedModules, Map<String, Long> counts) {

    public RestoreResult {
        restoredModules = List.copyOf(restoredModules);
        skippedModules = List.copyOf(skippedModules);
        counts = Map.copyOf(counts);
    }
}
