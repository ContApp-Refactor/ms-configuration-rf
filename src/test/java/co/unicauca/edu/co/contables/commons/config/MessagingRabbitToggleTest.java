package co.unicauca.edu.co.contables.commons.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUsageInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.SpringEventsTaxUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.TaxUsageListener;

@SuppressWarnings("deprecation")
@DisplayName("Interruptor app.messaging.rabbit.enabled")
class MessagingRabbitToggleTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Listeners.class)
            .withBean(ITaxUsageInputPort.class, () -> mock(ITaxUsageInputPort.class));

    @Configuration
    @Import({ TaxUsageListener.class, SpringEventsTaxUsageListener.class })
    static class Listeners {
    }

    @Test
    @DisplayName("Propiedad ausente: solo el listener en proceso")
    void defaultLoadsOnlySpringEvents() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(SpringEventsTaxUsageListener.class);
            assertThat(ctx).doesNotHaveBean(TaxUsageListener.class);
        });
    }

    @Test
    @DisplayName("enabled=false: solo el listener en proceso")
    void explicitFalseLoadsOnlySpringEvents() {
        runner.withPropertyValues("app.messaging.rabbit.enabled=false").run(ctx -> {
            assertThat(ctx).hasSingleBean(SpringEventsTaxUsageListener.class);
            assertThat(ctx).doesNotHaveBean(TaxUsageListener.class);
        });
    }

    @Test
    @DisplayName("enabled=true: solo el listener de Rabbit")
    void enabledTrueLoadsOnlyRabbitListener() {
        runner.withPropertyValues("app.messaging.rabbit.enabled=true").run(ctx -> {
            assertThat(ctx).hasSingleBean(TaxUsageListener.class);
            assertThat(ctx).doesNotHaveBean(SpringEventsTaxUsageListener.class);
        });
    }

    @Test
    @DisplayName("Valor inválido: ambos lados apagados (fail-closed)")
    void invalidValueDisablesBoth() {
        runner.withPropertyValues("app.messaging.rabbit.enabled=yes").run(ctx -> {
            assertThat(ctx).doesNotHaveBean(TaxUsageListener.class);
            assertThat(ctx).doesNotHaveBean(SpringEventsTaxUsageListener.class);
        });
    }
}
