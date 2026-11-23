package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.config.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.config.RabbitBankAccountUsedConfig;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto.BankAccountUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountUsage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Listener para eventos de uso de cuentas bancarias
 *
 * Escucha eventos de RabbitMQ cuando se notifica el uso de una cuenta bancaria,
 * actualizando el contador de uso correspondiente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BankAccountUsageListener extends AbstractMessageListener<EventDto<BankAccountUsageDto, EventUsageType>> {

    private final IBankAccountUsage bankAccountUsage;

    /**
     * @brief Maneja eventos de uso de cuentas bancarias desde la cola
     * @param event Evento con información de la cuenta bancaria usada
     * @param message El mensaje RabbitMQ raw
     * @param channel El canal RabbitMQ
     * @param deliveryTag El tag de entrega del mensaje
     */
    @RabbitListener(queues = RabbitBankAccountUsedConfig.BANK_ACCOUNT_USED_QUEUE)
    public void handleBankAccountEvent(
        EventDto<BankAccountUsageDto, EventUsageType> event,
        Message message,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Procesa el evento de cuenta bancaria basado en su tipo
     * @param event El evento de cuenta bancaria a procesar
     */
    @Override
    protected void processEvent(EventDto<BankAccountUsageDto, EventUsageType> event) {
        try {
            switch (event.getType()) {
                case USED:

                    if (!isValidEvent(event)) {
                        log.warn("Invalid bank account usage event received");
                        return;
                    }

                    BankAccountUsageDto data = event.getData();
                    log.info("Registrando uso de cuenta bancaria ID: {}, empresa: {}, cantidad: {}",
                             data.getBankAccountId(), data.getEnterpriseId(), data.getQuantityUsed());
                    bankAccountUsage.incrementUsageCount(data.getBankAccountId(), data.getEnterpriseId());
                    log.info("Uso registrado correctamente para cuenta bancaria ID: {}", data.getBankAccountId());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de evento no soportado: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando evento de cuenta bancaria: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * @brief Valida la integridad de los datos del evento de cuenta bancaria
     * @param event El evento de cuenta bancaria a validar
     * @return True si el evento es válido, false en caso contrario
     */
    @Override
    protected boolean isValidEvent(EventDto<BankAccountUsageDto, EventUsageType> event) {
        if (event == null) {
            log.warn("Evento es null");
            return false;
        }

        if (event.getType() == null) {
            log.warn("Tipo de evento es null");
            return false;
        }

        if (event.getData() == null) {
            log.warn("Datos del evento son null");
            return false;
        }

        BankAccountUsageDto data = event.getData();

        if (data.getBankAccountId() == null) {
            log.warn("BankAccountId es null - campo obligatorio");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            log.warn("EnterpriseId es null o vacío - campo obligatorio");
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed es null o inválido - campo obligatorio");
            return false;
        }

        return true;
    }

    @Override
    protected String getEntityType() {
        return "BankAccount";
    }

    @Override
    protected String extractEventType(EventDto<BankAccountUsageDto, EventUsageType> event) {
        // Este método se requiere por la interfaz AbstractMessageListener
        // pero no se utiliza en el contexto de BankAccountUsageListener
        // ya que no se implementa manejo de errores en base de datos
        return null;
    }

    @Override
    protected String convertEventToJson(EventDto<BankAccountUsageDto, EventUsageType> event) {
        // Este método se requiere por la interfaz AbstractMessageListener
        // pero no se utiliza en el contexto de BankAccountUsageListener
        // ya que no se implementa manejo de errores en base de datos
        return "{}";
    }
}
