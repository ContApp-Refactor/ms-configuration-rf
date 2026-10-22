package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @brief Repository interface for managing Message Processing Error entities.
 * This interface extends JpaRepository to provide CRUD operations for MessageProcessingErrorEntity.
 */
public interface IMessageProcessingErrorRepository extends JpaRepository<MessageProcessingErrorEntity, Long> {

    /**
     * Finds the most recent MessageProcessingErrorEntity based on the highest ID.
     *
     * @return An Optional containing the latest MessageProcessingErrorEntity if found, otherwise empty.
     */
    Optional<MessageProcessingErrorEntity> findFirstByOrderByIdDesc();
}
