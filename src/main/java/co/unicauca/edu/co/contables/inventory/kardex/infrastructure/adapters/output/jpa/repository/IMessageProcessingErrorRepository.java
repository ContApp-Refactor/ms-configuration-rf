package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;

/**
 * Repositorio para gestionar los errores de procesamiento de mensajes.
 */
@Repository("kardexMessageProcessingErrorRepository")
public interface IMessageProcessingErrorRepository extends JpaRepository<MessageProcessingErrorEntity, Long> {
    
    /**
     * @brief Finds the most recent message processing error ordered by error timestamp
     * @return Optional containing the latest error record if found
     */
    Optional<MessageProcessingErrorEntity> findFirstByOrderByErrorTimestampDesc();
}
