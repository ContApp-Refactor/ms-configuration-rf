package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.MessageErrorEntity;

public interface IMessageErrorRepository extends JpaRepository<MessageErrorEntity, Long> {

    /**
     * @brief Finds the most recent message processing error ordered by error timestamp
     * @return Optional containing the latest error record if found
     */
    Optional<MessageErrorEntity> findFirstByOrderByErrorAtDesc();

}
