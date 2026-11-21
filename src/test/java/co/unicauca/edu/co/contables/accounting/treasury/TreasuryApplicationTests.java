package co.unicauca.edu.co.contables.accounting.treasury;

import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.PurchaseInvoiceOutboxService;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.SpringEventsPurchaseInvoiceOutboxService;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.ReceiptPublisher;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.SpringEventsReceiptPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryEventPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.SpringEventsTreasuryAccountingListener;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.SpringEventsTreasuryEventListener;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker.SpringEventsTreasuryAccountingResultPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker.SpringEventsTreasuryEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Safety net for the monolith startup: the only test that loads the full
 * ConfigurationApplication.
 *
 * Two test-scoped overrides are required, neither of which may be fixed in main
 * code because both belong to modules that are already closed:
 *
 * 1. "@ActiveProfiles(\"test\")" routes the datasource to the H2 instance declared
 *    in src/test/resources/application-test.yml. Without it the context tries to
 *    reach the unpublished general_config database and fails with "Unable to
 *    determine Dialect without JDBC metadata".
 * 2. GeographyDataInitializer is mocked out and spring.sql.init is disabled,
 *    because both seed scripts are PostgreSQL-only: they use
 *    "ON CONFLICT ... DO NOTHING", which H2 2.3.232 rejects in EVERY mode
 *    (verified empirically: regular, MODE=PostgreSQL and MODE=PostgreSQL with
 *    DATABASE_TO_LOWER all fail). H2 cannot execute this dialect, so the seed
 *    path cannot be exercised here without editing the thirds module.
 *
 * What this test still guarantees: every Spring bean, mapper, security rule,
 * JPA mapping and Rabbit topology of the monolith resolves and wires.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@SuppressWarnings("deprecation")
class TreasuryApplicationTests {

	@MockitoBean
	private GeographyDataInitializer geographyDataInitializer;

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
	}

	@Test
	void purchaseInvoiceAndResultTopologyAreGated() {
		assertThat(context.containsBean("purchaseInvoiceExchange")).isFalse();
		assertThat(context.containsBean("treasuryInvoiceExchange")).isFalse();
		assertThat(context.containsBean("purchaseQueue")).isFalse();
		assertThat(context.containsBean("purchaseBinding")).isFalse();
		assertThat(context.containsBean("purchaseInvoiceEventPublisher")).isFalse();

		assertThat(context.containsBean("resultExchange")).isFalse();
		assertThat(context.containsBean("resultQueue")).isFalse();
		assertThat(context.containsBean("resultBinding")).isFalse();
		assertThat(context.containsBean("treasuryDlx")).isFalse();
		assertThat(context.containsBean("treasuryDlq")).isFalse();
		assertThat(context.containsBean("dlqBinding")).isFalse();
		assertThat(context.containsBean("notificationExchange")).isFalse();
		assertThat(context.containsBean("treasuryRabbitListenerContainerFactory")).isFalse();

		assertThat(context.containsBean("treasuryEventListener")).isFalse();
		assertThat(context.containsBean("treasuryAccountingResultPublisher")).isFalse();

		assertThat(context.getBean(PurchaseInvoiceOutboxService.class))
				.isInstanceOf(SpringEventsPurchaseInvoiceOutboxService.class);
		assertThat(context.getBean(ReceiptPublisher.class))
				.isInstanceOf(SpringEventsReceiptPublisher.class);
	}

	@Test
	void treasuryLoopsUseSpringEvents() {
		assertThat(context.containsBean("treasuryAccountingQueue")).isFalse();
		assertThat(context.containsBean("treasuryAccountingBinding")).isFalse();
		assertThat(context.containsBean("accountingExchange")).isFalse();
		assertThat(context.containsBean("outboxEventAdapter")).isFalse();
		assertThat(context.containsBean("treasuryOutboxMessenger")).isFalse();
		assertThat(context.containsBean("treasuryAccountingListener")).isFalse();

		assertThat(context.getBean(ITreasuryEventPublisher.class))
				.isInstanceOf(SpringEventsTreasuryEventPublisher.class);
		assertThat(context.getBean(SpringEventsTreasuryAccountingListener.class)).isNotNull();
		assertThat(context.getBean(SpringEventsTreasuryEventListener.class)).isNotNull();
		assertThat(context.getBean(ITreasuryAccountingResultPublisher.class))
				.isInstanceOf(SpringEventsTreasuryAccountingResultPublisher.class);
	}
}
