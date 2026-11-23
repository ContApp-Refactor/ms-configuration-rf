package co.unicauca.edu.co.contables.financial;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class FinancialStatementsApplicationTests {

	@MockitoBean
	private GeographyDataInitializer geographyDataInitializer;

	@Test
	void contextLoads() {
	}
}