package co.unicauca.edu.co.contables.billing;

import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingIdGeneratorPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingTransactionRunnerPort;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * TEN-2: el contexto completo arranca con billing y el legado conviviendo.
 * Mismas anotaciones que TreasuryApplicationTests para reutilizar el contexto cacheado.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class BillingContextSmokeTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private BillingExecutionContextPort executionContext;

    @Autowired
    private BillingClockPort clock;

    @Autowired
    private BillingIdGeneratorPort idGenerator;

    @Autowired
    private BillingTransactionRunnerPort transactionRunner;

    @Autowired
    private BillingProperties properties;

    @Test
    void TEN_2_context_boots_with_billing_beans_and_legacy_present() {
        assertThat(executionContext).isNotNull();
        assertThat(clock).isNotNull();
        assertThat(idGenerator).isNotNull();
        assertThat(transactionRunner).isNotNull();
    }

    @Test
    void default_properties_are_applied_without_yml() {
        assertThat(properties.getDefaultVatRate()).isEqualByComparingTo(BigDecimal.valueOf(19));
        assertThat(properties.getTimeZone()).isEqualTo("America/Bogota");
        assertThat(properties.getOutbox().getMaxAttempts()).isEqualTo(10);
        assertThat(properties.getOutbox().getBatchSize()).isEqualTo(50);
    }
}
