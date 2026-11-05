package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

/**
 * Declara únicamente el exchange de facturas de compra, que es exclusivo de
 * este módulo.
 *
 * La topología de `invoice.exchange` / `invoice.payments.queue` /
 * `invoice.accounting.queue` NO se redeclara acá: ya la declaran
 * `accounting.debt_payments...RabbitInvoiceConfig` y
 * `accounting.catalogue...RabbitConfig`, y volver a declararla sin los
 * argumentos `x-dead-letter-exchange` provoca primero
 * `BeanDefinitionOverrideException` y después `PRECONDITION_FAILED` en el
 * broker.
 */
@Configuration
@Slf4j
@Profile("!test")
public class RabbitReceiptConfig {
    public static final String PURCHASE_INVOICE_EXCHANGE = "purchase.invoice.exchange";

    @Bean
    FanoutExchange purchaseInvoiceExchange() {
        return new FanoutExchange(PURCHASE_INVOICE_EXCHANGE, true, false);
    }
}
