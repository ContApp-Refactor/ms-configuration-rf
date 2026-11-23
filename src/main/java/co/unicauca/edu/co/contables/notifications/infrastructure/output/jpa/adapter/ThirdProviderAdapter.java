package co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.notifications.application.output.IThirdProviderPort;
import co.unicauca.edu.co.contables.notifications.domain.model.ThirdReplica;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Adapter class for accessing ThirdReplica data from the database.
 * Implements the IThirdProviderPort interface.
 * Uses the IThirdReplicaRepository to fetch ThirdReplica entities.
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class ThirdProviderAdapter implements IThirdProviderPort {

    private final IThirdReplicaRepository thirdReplicaRepository;

    /**
     * @brief Finds a ThirdReplica by its third party ID.
     * @param thirdPartyId the third party ID to search for 
     */
    @Override
    public Optional<ThirdReplica> findByThirdPartyId(Long thirdPartyId) {
        log.info("Buscando tercero con ID: {}", thirdPartyId);
        return thirdReplicaRepository.findByThirdId(thirdPartyId)
                .map(entity -> new ThirdReplica(
                    entity.getId(),
                    entity.getThirdId(),
                    entity.getFullName(),
                    entity.getEmail()
                ));
    }
    
}
