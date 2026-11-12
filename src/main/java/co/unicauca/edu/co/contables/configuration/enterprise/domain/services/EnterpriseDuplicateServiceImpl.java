package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupProgressListener;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.IEnterpriseBackupCoordinator;
import lombok.RequiredArgsConstructor;

/**
 * @brief Duplicar empresa: delega en el coordinador (export a ZIP temporal +
 * restore DUPLICATE, temporal borrado siempre). Es el mismo camino que usa el
 * proceso de copia DUPLICATE; ya no hay dos implementaciones del flujo.
 */
@Service
@RequiredArgsConstructor
public class EnterpriseDuplicateServiceImpl implements IEnterpriseDuplicateService {

    private final IEnterpriseBackupCoordinator coordinator;

    @Override
    public UUID duplicate(UUID enterpriseId, String targetName, RestoreActor actor) {
        return coordinator.duplicate(enterpriseId, targetName, actor, null, BackupProgressListener.NONE)
                .enterpriseId();
    }
}
