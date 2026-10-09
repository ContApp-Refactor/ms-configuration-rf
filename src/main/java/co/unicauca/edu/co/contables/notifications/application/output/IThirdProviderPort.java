package co.unicauca.edu.co.contables.notifications.application.output;

import java.util.Optional;

import co.unicauca.edu.co.contables.notifications.domain.model.ThirdReplica;

/**
 * @brief Port interface for accessing third party data.
 * Defines the contract for third party data retrieval services.
 */
public interface IThirdProviderPort {
    Optional<ThirdReplica> findByThirdPartyId(Long thirdPartyId);
}
