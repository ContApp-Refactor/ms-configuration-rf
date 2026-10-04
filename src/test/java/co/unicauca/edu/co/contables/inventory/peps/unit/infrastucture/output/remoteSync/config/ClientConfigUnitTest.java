package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.unicauca.edu.co.contables.commons.config.aspect.JwtTokenService;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config.ClientConfig;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config.ClientProperties;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config.IKardexExternalClient;

/**
 * @brief Unit tests for ClientConfig
 *
 * Tras la migración, {@code ClientConfig} solo declara un bean:
 * {@code kardexExternalClient}. Los clientes de stock, products y config
 * desaparecieron porque esos módulos ahora son in-process, y el proxy se
 * construye con {@code RestClient} porque el monolito no lleva spring-webflux.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientConfig Unit Tests")
class ClientConfigUnitTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private ClientConfig clientConfig;

    @Nested
    @DisplayName("Structure Tests")
    class StructureTests {

        @Test
        @DisplayName("Should be a Spring @Configuration class")
        void shouldBeConfigurationClass() {
            assertTrue(ClientConfig.class.isAnnotationPresent(Configuration.class));
        }

        @Test
        @DisplayName("Should register ClientProperties with @EnableConfigurationProperties")
        void shouldEnableClientProperties() {
            EnableConfigurationProperties annotation =
                ClientConfig.class.getAnnotation(EnableConfigurationProperties.class);
            assertNotNull(annotation);
            assertEquals(1, annotation.value().length);
            assertEquals(ClientProperties.class, annotation.value()[0]);
        }

        @Test
        @DisplayName("Should expose exactly one @Bean method: kardexExternalClient")
        void shouldHaveExactlyOneBeanMethod() throws Exception {
            long beanMethods = java.util.Arrays.stream(ClientConfig.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .count();

            assertEquals(1, beanMethods);

            Method method = ClientConfig.class.getDeclaredMethod("kardexExternalClient", ClientProperties.class);
            assertTrue(method.isAnnotationPresent(Bean.class), "kardexExternalClient must be a @Bean");
            assertFalse(Modifier.isPrivate(method.getModifiers()),
                "Spring cannot invoke private @Bean methods");
            assertEquals(IKardexExternalClient.class, method.getReturnType());
        }

        @Test
        @DisplayName("Should no longer declare the stock/products/config clients")
        void shouldNotDeclareRemovedClientBeans() {
            for (String removed : new String[] {"stockClient", "productClient", "configClient"}) {
                assertThrows(NoSuchMethodException.class,
                    () -> ClientConfig.class.getDeclaredMethod(removed),
                    removed + " must be gone: that module is in-process now");
            }
        }
    }

    @Nested
    @DisplayName("Bean creation Tests")
    class BeanCreationTests {

        /**
         * El método @Bean es de paquete (Spring lo acepta), así que se invoca por
         * reflexión desde este test, que vive en otro paquete.
         */
        private Object invokeKardexExternalClient(ClientProperties properties) throws Exception {
            Method method = ClientConfig.class.getDeclaredMethod("kardexExternalClient", ClientProperties.class);
            method.setAccessible(true);
            return method.invoke(clientConfig, properties);
        }

        @Test
        @DisplayName("Should create the kardex-external proxy when a base URL is configured")
        void shouldCreateKardexExternalClient() throws Exception {
            ClientProperties properties = new ClientProperties();
            properties.getKardexExternal().setBaseUrl("http://inventory-weighted-average-rf:8080");

            assertNotNull(invokeKardexExternalClient(properties));
        }

        @Test
        @DisplayName("Should reject a blank base URL")
        void shouldRejectBlankBaseUrl() {
            ClientProperties properties = new ClientProperties();
            properties.getKardexExternal().setBaseUrl("   ");

            InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                () -> invokeKardexExternalClient(properties));

            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertTrue(thrown.getCause().getMessage().contains("IKardexExternalClient"));
        }

        @Test
        @DisplayName("Should reject a null base URL")
        void shouldRejectNullBaseUrl() {
            ClientProperties properties = new ClientProperties();
            properties.getKardexExternal().setBaseUrl(null);

            InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                () -> invokeKardexExternalClient(properties));

            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
        }
    }
}
