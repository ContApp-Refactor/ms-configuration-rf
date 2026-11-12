package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.UUID;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;

/**
 * @brief Puerto de salida para duplicar una empresa.
 *
 * DUPLICATE no es un mecanismo aparte: es un export completo del origen
 * encadenado con un restore en modo DUPLICATE (empresa nueva, NIT repetido
 * permitido), todo en {@code IEnterpriseBackupCoordinator}.
 */
public interface IEnterpriseDuplicateService {

    /** Duplica con el usuario/tenant de la petición actual y el nombre {@code "<nombre> (copia)"}. */
    default UUID duplicate(UUID enterpriseId) {
        return duplicate(enterpriseId, null, RestoreActor.fromCurrentContext());
    }

    /**
     * @param targetName nombre de la empresa nueva; {@code null} = {@code "<nombre> (copia)"}
     * @return el id de la empresa nueva
     */
    UUID duplicate(UUID enterpriseId, String targetName, RestoreActor actor);
}
