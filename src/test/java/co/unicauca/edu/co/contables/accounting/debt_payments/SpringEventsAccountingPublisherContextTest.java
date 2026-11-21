package co.unicauca.edu.co.contables.accounting.debt_payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener.SpringEventsReceiptMessageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener.SpringEventsWriteOffMessageListener;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IAccountingEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.SpringEventsAccountingEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@DisplayName("Cells 18/19 — active profile wiring")
class SpringEventsAccountingPublisherContextTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("only the accounting publisher and receipt/write-off listeners are active")
    void shouldWireOnlySpringEventsImplementations() {
        Map<String, IAccountingEventPublisher> publishers = context.getBeansOfType(IAccountingEventPublisher.class);

        assertThat(publishers).hasSize(1);
        assertThat(publishers.values().iterator().next())
                .isInstanceOf(SpringEventsAccountingEventPublisher.class);
        assertThat(context.containsBean("accountingEventPublisher")).isFalse();
        assertThat(context.containsBean("receiptMessageListener")).isFalse();
        assertThat(context.containsBean("writeOffMessageListener")).isFalse();
        assertThat(context.containsBean("rabbitAccountingConfig")).isFalse();
        assertThat(context.getBean(SpringEventsReceiptMessageListener.class)).isNotNull();
        assertThat(context.getBean(SpringEventsWriteOffMessageListener.class)).isNotNull();
    }
}
