package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import java.util.List;

/**
 * @brief Módulo que esta versión sabe exportar/restaurar ({@code GET /api/enterprises/copy/modules}).
 */
public record BackupModuleInfo(String key, int order, int schemaVersion, List<String> requires) {

    public BackupModuleInfo {
        requires = List.copyOf(requires);
    }
}
