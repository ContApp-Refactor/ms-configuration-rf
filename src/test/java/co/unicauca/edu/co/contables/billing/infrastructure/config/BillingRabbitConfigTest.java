package co.unicauca.edu.co.contables.billing.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * EVT-5, D14: topologia RabbitMQ de facturacion. Un exchange topic durable con exchange alterno
 * (fanout) que enruta a una cola de no enrutados, mas DLX/DLQ para las colas de los consumidores.
 * Solo nombres {@code billing.*}: nunca {@code invoice.exchange} ni {@code purchase.invoice.exchange}.
 * Se prueba instanciando la configuracion (los beans son metodos puros) y por reflexion.
 */
class BillingRabbitConfigTest {

    private final BillingRabbitConfig config = new BillingRabbitConfig();

    @Test
    void EVT_5_documents_exchange_is_a_durable_topic_with_the_unrouted_alternate_exchange() {
        Exchange exchange = config.billingDocumentsExchange();

        assertThat(exchange.getName()).isEqualTo("billing.documents.exchange");
        assertThat(exchange.getType()).isEqualTo(ExchangeTypes.TOPIC);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
        assertThat(exchange.getArguments()).containsEntry("alternate-exchange", "billing.documents.unrouted.exchange");
    }

    @Test
    void EVT_5_unrouted_exchange_is_a_durable_fanout_bound_to_the_unrouted_queue() {
        Exchange exchange = config.billingUnroutedExchange();
        Queue queue = config.billingUnroutedQueue();
        Binding binding = config.billingUnroutedBinding();

        assertThat(exchange.getName()).isEqualTo("billing.documents.unrouted.exchange");
        assertThat(exchange.getType()).isEqualTo(ExchangeTypes.FANOUT);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(queue.getName()).isEqualTo("billing.documents.unrouted.queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(binding.getExchange()).isEqualTo("billing.documents.unrouted.exchange");
        assertThat(binding.getDestination()).isEqualTo("billing.documents.unrouted.queue");
        assertThat(binding.getDestinationType()).isEqualTo(Binding.DestinationType.QUEUE);
    }

    @Test
    void EVT_5_dead_letter_exchange_is_a_durable_fanout_bound_to_the_dlq() {
        Exchange exchange = config.billingDeadLetterExchange();
        Queue queue = config.billingDeadLetterQueue();
        Binding binding = config.billingDeadLetterBinding();

        assertThat(exchange.getName()).isEqualTo("billing.documents.dlx");
        assertThat(exchange.getType()).isEqualTo(ExchangeTypes.FANOUT);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(queue.getName()).isEqualTo("billing.documents.dlq");
        assertThat(queue.isDurable()).isTrue();
        assertThat(binding.getExchange()).isEqualTo("billing.documents.dlx");
        assertThat(binding.getDestination()).isEqualTo("billing.documents.dlq");
    }

    @Test
    void EVT_5_the_alternate_exchange_is_not_the_dead_letter_exchange() {
        assertThat(config.billingDocumentsExchange().getArguments().get("alternate-exchange"))
                .isNotEqualTo(config.billingDeadLetterExchange().getName());
    }

    @Test
    void EVT_5_no_declared_name_collides_with_the_legacy_invoice_topology() {
        List<Declarable> declared = List.of(config.billingDocumentsExchange(), config.billingUnroutedExchange(),
                config.billingUnroutedQueue(), config.billingUnroutedBinding(), config.billingDeadLetterExchange(),
                config.billingDeadLetterQueue(), config.billingDeadLetterBinding());
        String names = declared.stream().map(d -> {
            if (d instanceof Exchange e) {
                return e.getName();
            }
            if (d instanceof Queue q) {
                return q.getName();
            }
            Binding b = (Binding) d;
            return b.getExchange() + ">" + b.getDestination();
        }).reduce("", (a, b) -> a + " " + b);

        // "invoice.exchange" tambien cubre "purchase.invoice.exchange"
        assertThat(names).doesNotContain("invoice.exchange").doesNotContain("invoice.payments");
        declared.forEach(d -> {
            if (d instanceof Exchange e) {
                assertThat(e.getName()).startsWith("billing.");
            } else if (d instanceof Queue q) {
                assertThat(q.getName()).startsWith("billing.");
            }
        });
    }

    @Test
    void EVT_5_exactly_seven_beans_all_with_the_billing_prefix() {
        List<Method> beans = Arrays.stream(BillingRabbitConfig.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .toList();

        assertThat(beans).hasSize(7);
        assertThat(beans).allSatisfy(m -> assertThat(m.getName()).startsWith("billing"));
    }

    @Test
    void D18_the_configuration_is_excluded_from_the_test_profile_and_has_a_billing_bean_name() {
        Profile profile = BillingRabbitConfig.class.getAnnotation(Profile.class);
        Configuration configuration = BillingRabbitConfig.class.getAnnotation(Configuration.class);

        assertThat(profile).isNotNull();
        assertThat(profile.value()).containsExactly("!test");
        assertThat(configuration.value()).isEqualTo("billingRabbitConfig");
    }
}
