package co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.MessageProcessingErrorEntity;



/**
 * @brief Repository interface for managing Message Processing Error entities.
 * This interface extends JpaRepository to provide CRUD operations for MessageProcessingErrorEntity.
 */
@Repository("notificationsMessageProcessingErrorRepository")
public interface IMessageProcessingErrorRepository extends JpaRepository<MessageProcessingErrorEntity, Long> {

    /**
     * Finds the most recent MessageProcessingErrorEntity based on the highest ID.
     *
     * @return An Optional containing the latest MessageProcessingErrorEntity if found, otherwise empty.
     */
    Optional<MessageProcessingErrorEntity> findFirstByOrderByIdDesc();
}