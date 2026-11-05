package co.unicauca.edu.co.contables.keycloak;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Red de seguridad del arranque con el módulo keycloak cargado: mismo patrón
 * que {@code AuditApplicationTests} (perfil test + H2 + GeographyDataInitializer
 * mockeado). Las claves {@code keycloak.*}, {@code app.*} y
 * {@code jwt.public.key/token.url} viven en {@code application.yml}, que el
 * perfil test sí carga.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class KeycloakApplicationTests {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Test
    void contextLoads() {
    }
}
