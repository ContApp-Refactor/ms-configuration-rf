package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.config.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.used.RabbitPaymentMethodConfig;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.dto.PaymentMethodUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodUsage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Listener para eventos de uso de métodos de pago
 *
 * Escucha eventos de RabbitMQ cuando se notifica el uso de un método de pago,
 * actualizando el contador de uso correspondiente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodUsageListener extends AbstractMessageListener<EventDto<PaymentMethodUsageDto, EventUsageType>> {

    private final IPaymentMethodUsage paymentMethodUsagePort;

    /**
     * @brief Maneja eventos de uso de métodos de pago desde la cola
     * @param event Evento con información del método de pago usado
     * @param message El mensaje RabbitMQ raw
     * @param channel El canal RabbitMQ
     * @param deliveryTag El tag de entrega del mensaje
     */
    @RabbitListener(queues = RabbitPaymentMethodConfig.PAYMENTMETHOD_USED_QUEUE)
    public void handlePaymentMethodEvent(
        EventDto<PaymentMethodUsageDto, EventUsageType> event,
        Message message,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Procesa el evento de método de pago basado en su tipo
     * @param event El evento de método de pago a procesar
     */
    @Override
    protected void processEvent(EventDto<PaymentMethodUsageDto, EventUsageType> event) {
        try {
            switch (event.getType()) {
                case USED:

                    if (!isValidEvent(event)) {
                        log.warn("Invalid payment method usage event received");
                        return;
                    }

                    PaymentMethodUsageDto data = event.getData();
                    log.info("Registrando uso de método de pago ID: {}, empresa: {}, cantidad: {}",
                             data.getPaymentMethodId(), data.getEnterpriseId(), data.getQuantityUsed());
                    paymentMethodUsagePort.incrementUsageCount(data.getPaymentMethodId(), data.getEnterpriseId());
                    log.info("Uso registrado correctamente para método de pago ID: {}", data.getPaymentMethodId());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de evento no soportado: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Error procesando evento de método de pago: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * @brief Valida la integridad de los datos del evento de método de pago
     * @param event El evento de método de pago a validar
     * @return True si el evento es válido, false en caso contrario
     */
    @Override
    protected boolean isValidEvent(EventDto<PaymentMethodUsageDto, EventUsageType> event) {
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

        PaymentMethodUsageDto data = event.getData();

        if (data.getPaymentMethodId() == null) {
            log.warn("PaymentMethodId es null - campo obligatorio");
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
        return "PaymentMethod";
    }

    @Override
    protected String extractEventType(EventDto<PaymentMethodUsageDto, EventUsageType> event) {
        // Este método se requiere por la interfaz AbstractMessageListener
        // pero no se utiliza en el contexto de PaymentMethodUsageListener
        // ya que no se implementa manejo de errores en base de datos
        return null;
    }

    @Override
    protected String convertEventToJson(EventDto<PaymentMethodUsageDto, EventUsageType> event) {
        // Este método se requiere por la interfaz AbstractMessageListener
        // pero no se utiliza en el contexto de PaymentMethodUsageListener
        // ya que no se implementa manejo de errores en base de datos
        return "{}";
    }
}
