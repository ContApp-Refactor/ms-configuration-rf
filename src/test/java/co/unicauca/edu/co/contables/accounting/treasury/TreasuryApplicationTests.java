package co.unicauca.edu.co.contables.accounting.treasury;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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
class TreasuryApplicationTests {

	@MockitoBean
	private GeographyDataInitializer geographyDataInitializer;

	@Test
	void contextLoads() {
	}

}
