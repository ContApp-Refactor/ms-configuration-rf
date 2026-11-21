package co.unicauca.edu.co.contables.commons.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.ReflectionUtils;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.RabbitInvoiceConfig;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.SpringEventsTreasuryEventListener;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker.SpringEventsTreasuryAccountingResultPublisher;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.scheduler.BillingOutboxRelayScheduler;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.transaction.BillingOutboxRelayUseCaseAdapter;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.BillingRabbitEventPublisher;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingRabbitConfig;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@DisplayName("Phase 10 — Rabbit gate audit")
class MessagingGateAuditTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Ningun bean activo declara @RabbitListener")
    void noActiveBeanDeclaresRabbitListener() {
        List<String> offenders = new ArrayList<>();
        for (Object bean : context.getBeansOfType(Object.class).values()) {
            Class<?> target = AopUtils.getTargetClass(bean);
            if (target == null) {
                continue;
            }
            ReflectionUtils.doWithMethods(target, method -> {
                if (AnnotatedElementUtils.hasAnnotation(method, RabbitListener.class)) {
                    offenders.add(target.getName() + "#" + method.getName());
                }
            });
        }
        assertThat(offenders).isEmpty();
    }

    @Test
    @DisplayName("Los beans Rabbit representativos estan apagados")
    void representativeRabbitBeansAreOff() {
        assertThat(context.containsBean("treasuryEventListener")).isFalse();
        assertThat(context.containsBean("treasuryAccountingResultPublisher")).isFalse();
        assertThat(context.containsBean("taxUsageListener")).isFalse();
        assertThat(context.containsBean("rabbitTaxUsedConfig")).isFalse();
        assertThat(context.containsBean("jwtContextRabbitMqAspect")).isFalse();
    }

    @Test
    @DisplayName("Las contrapartes en proceso estan activas")
    void springEventCounterpartsAreActive() {
        assertThat(context.getBean(SpringEventsTreasuryEventListener.class)).isNotNull();
        assertThat(context.getBean(SpringEventsTreasuryAccountingResultPublisher.class)).isNotNull();
    }

    @Test
    @DisplayName("Billing y la infraestructura compartida no llevan la condicion")
    void billingAndSharedInfrastructureAreNotPropertyGated() {
        for (Class<?> type : List.of(
                BillingRabbitConfig.class,
                BillingRabbitEventPublisher.class,
                BillingOutboxRelayScheduler.class,
                BillingOutboxRelayUseCaseAdapter.class,
                RabbitCommonConfig.class,
                RabbitInvoiceConfig.class)) {
            assertThat(gatedByRabbitSwitch(type))
                    .as("%s must not be gated by app.messaging.rabbit.enabled", type.getName())
                    .isFalse();
        }

        Method rabbitTemplate = ReflectionUtils.findMethod(
                RabbitInvoiceConfig.class, "rabbitTemplate",
                ConnectionFactory.class, Jackson2JsonMessageConverter.class);
        assertThat(rabbitTemplate).isNotNull();
        assertThat(AnnotatedElementUtils.hasAnnotation(rabbitTemplate, ConditionalOnProperty.class))
                .isFalse();
    }

    private static boolean gatedByRabbitSwitch(Class<?> type) {
        ConditionalOnProperty condition = type.getAnnotation(ConditionalOnProperty.class);
        if (condition == null) {
            return false;
        }
        String prefix = condition.prefix().isEmpty() ? "" : condition.prefix() + ".";
        String[] names = condition.name().length > 0 ? condition.name() : condition.value();
        for (String name : names) {
            if ("app.messaging.rabbit.enabled".equals(prefix + name)) {
                return true;
            }
        }
        return false;
    }
}
