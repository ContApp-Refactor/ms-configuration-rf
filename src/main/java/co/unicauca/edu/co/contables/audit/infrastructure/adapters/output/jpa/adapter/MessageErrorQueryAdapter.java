package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.audit.application.dto.response.MessageErrorAuditResponse;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorQueryPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.MessageErrorEntity;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository.IMessageErrorRepository;

import lombok.RequiredArgsConstructor;

// Nombre de bean explicito: debt_payments e inventoryWeightedAverage definen
// adaptadores con el mismo nombre por defecto y Spring aborta con
// ConflictingBeanDefinitionException (mismo motivo que
// auditMessageErrorHandlingAdapter).
@Repository("auditMessageErrorQueryAdapter")
@RequiredArgsConstructor
public class MessageErrorQueryAdapter implements IMessageErrorQueryPort {

    private final IMessageErrorRepository messageRepository;

    @Override
    public Optional<MessageErrorAuditResponse> findLatest() {
        return messageRepository.findFirstByOrderByErrorAtDesc().map(this::toResponse);
    }

    @Override
    public void deleteAll() {
        messageRepository.deleteAll();
    }

    /**
     * @brief Proyecta la entidad a la forma plana que exige la coleccion
     *        newman. {@code errorAt} se serializa como texto ISO-8601.
     */
    private MessageErrorAuditResponse toResponse(MessageErrorEntity entity) {
        return MessageErrorAuditResponse.builder()
                .id(entity.getId())
                .eventType(entity.getEventType())
                .entityType(entity.getEntityType())
                .errorDescription(entity.getErrorDescription())
                .messageData(entity.getMessageData())
                .errorStage(entity.getErrorStage())
                .errorAt(entity.getErrorAt() == null ? null : entity.getErrorAt().toString())
                .build();
    }
}
