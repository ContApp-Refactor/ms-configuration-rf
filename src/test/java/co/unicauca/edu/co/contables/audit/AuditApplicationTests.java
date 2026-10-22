package co.unicauca.edu.co.contables.audit;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Red de seguridad del arranque con el módulo audit cargado: mismo patrón que
 * TreasuryApplicationTests, FinancialStatementsApplicationTests y
 * MsNotificationsApplicationTests (perfil test + H2 + GeographyDataInitializer
 * mockeado). Como la configuración es idéntica, Spring Test reutiliza el
 * contexto ya cacheado.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class AuditApplicationTests {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Test
    void contextLoads() {
    }
}
