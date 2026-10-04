package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.config;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config.IKardexExternalClient;

/**
 * Unit tests for {@link IKardexExternalClient}
 * 
 * Tests the HTTP client interface structure and annotations for Kardex External Service integration.
 * Validates HTTP method declarations, parameter annotations, and return types.
 * 
 * @author Equipo-dinamita-escuadron-lobo
 */
@DisplayName("IKardexExternalClient Unit Tests")
public class IKardexExternalClientUnitTest {

    @Nested
    @DisplayName("Interface Structure Tests")
    class InterfaceStructureTests {

        @Test
        @DisplayName("Should be an interface")
        void shouldBeInterface() {
            // Arrange & Act & Assert
            assertTrue(IKardexExternalClient.class.isInterface(), 
                "IKardexExternalClient should be an interface");
        }

        @Test
        @DisplayName("Should be public")
        void shouldBePublic() {
            // Arrange & Act
            int modifiers = IKardexExternalClient.class.getModifiers();

            // Assert
            assertTrue(java.lang.reflect.Modifier.isPublic(modifiers), 
                "IKardexExternalClient should be public");
        }

        @Test
        @DisplayName("Should have exactly 1 method")
        void shouldHaveExactlyOneMethod() {
            // Arrange & Act
            Method[] methods = IKardexExternalClient.class.getDeclaredMethods();

            // Assert
            assertEquals(1, methods.length, 
                "IKardexExternalClient should have exactly 1 method");
        }
    }

    @Nested
    @DisplayName("FindKardexByEnterpriseId Method Tests")
    class FindKardexByEnterpriseIdMethodTests {

        @Test
        @DisplayName("Should have findKardexByEnterpriseId method")
        void shouldHaveFindKardexByEnterpriseIdMethod() {
            // Arrange & Act & Assert
            assertDoesNotThrow(() -> {
                IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            }, "IKardexExternalClient should have findKardexByEnterpriseId method");
        }

        @Test
        @DisplayName("Method should have GetExchange annotation")
        void methodShouldHaveGetExchangeAnnotation() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            boolean hasGetExchange = method.isAnnotationPresent(GetExchange.class);

            // Assert
            assertTrue(hasGetExchange, 
                "findKardexByEnterpriseId should have @GetExchange annotation");
        }

        @Test
        @DisplayName("GetExchange should have correct path")
        void getExchangeShouldHaveCorrectPath() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);

            // Assert
            assertNotNull(getExchange, "@GetExchange annotation should not be null");
            assertEquals("/api/kardex/weighted-average/last-kardex-all-products", getExchange.value(), 
                "GetExchange path should be '/api/kardex/weighted-average/last-kardex-all-products'");
        }

        @Test
        @DisplayName("Method should return KardexExternalResponseDTO")
        void methodShouldReturnKardexExternalResponseDTO() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            Class<?> returnType = method.getReturnType();
            String typeName = returnType.getSimpleName();

            // Assert
            assertEquals("KardexExternalResponseDTO", typeName, 
                "findKardexByEnterpriseId should return KardexExternalResponseDTO");
        }

        @Test
        @DisplayName("Method should have exactly 1 parameter")
        void methodShouldHaveExactlyOneParameter() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            var parameters = method.getParameters();

            // Assert
            assertEquals(1, parameters.length, 
                "findKardexByEnterpriseId should have exactly 1 parameter");
        }

        @Test
        @DisplayName("Parameter should have RequestParam annotation")
        void parameterShouldHaveRequestParamAnnotation() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            var parameters = method.getParameters();

            // Assert
            assertTrue(parameters[0].isAnnotationPresent(RequestParam.class), 
                "Parameter should have @RequestParam annotation");
        }

        @Test
        @DisplayName("Parameter should be String type (enterpriseId)")
        void parameterShouldBeStringType() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            var parameters = method.getParameters();

            // Assert
            assertEquals(String.class, parameters[0].getType(), 
                "Parameter should be of type String");
        }
    }

    @Nested
    @DisplayName("Method Return Type Tests")
    class MethodReturnTypeTests {

        @Test
        @DisplayName("Should return response DTO type")
        void shouldReturnResponseDtoType() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            Class<?> returnType = method.getReturnType();

            // Assert
            assertTrue(returnType.getSimpleName().contains("DTO"), 
                "Return type should be a DTO");
            assertTrue(returnType.getSimpleName().contains("Response"), 
                "Return type should be a Response DTO");
        }

        @Test
        @DisplayName("Return type package should be remoteSync dto")
        void returnTypePackageShouldBeRemoteSyncDto() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            Class<?> returnType = method.getReturnType();
            String packageName = returnType.getPackageName();

            // Assert
            assertTrue(packageName.contains("remoteSync"), 
                "Return type should be from remoteSync package");
            assertTrue(packageName.endsWith(".dto"), 
                "Return type should be from dto package");
        }
    }

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("Should use GET HTTP method")
        void shouldUseGetHttpMethod() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            boolean hasGetExchange = method.isAnnotationPresent(GetExchange.class);

            // Assert
            assertTrue(hasGetExchange, 
                "findKardexByEnterpriseId should use GET HTTP method (@GetExchange)");
        }

        @Test
        @DisplayName("GET method is appropriate for read-only retrieval")
        void getMethodIsAppropriateForReadOnlyRetrieval() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            String methodName = method.getName();
            boolean hasGetExchange = method.isAnnotationPresent(GetExchange.class);

            // Assert
            assertTrue(methodName.startsWith("find"), 
                "Method name starts with 'find' indicating retrieval");
            assertTrue(hasGetExchange, 
                "Retrieval operations should use GET HTTP method");
        }
    }

    @Nested
    @DisplayName("Method Naming Convention Tests")
    class MethodNamingConventionTests {

        @Test
        @DisplayName("Method name should be in camelCase")
        void methodNameShouldBeInCamelCase() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            String methodName = method.getName();

            // Assert
            assertEquals("findKardexByEnterpriseId", methodName, 
                "Method name should be 'findKardexByEnterpriseId' in camelCase");
        }

        @Test
        @DisplayName("Method name should follow find...By naming pattern")
        void methodNameShouldFollowFindByNamingPattern() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            String methodName = method.getName();

            // Assert
            assertTrue(methodName.startsWith("find"), 
                "Method name should start with 'find'");
            assertTrue(methodName.contains("By"), 
                "Method name should contain 'By' following repository naming pattern");
            assertTrue(methodName.endsWith("EnterpriseId"), 
                "Method name should end with the search criterion");
        }
    }

    @Nested
    @DisplayName("Parameter Annotation Tests")
    class ParameterAnnotationTests {

        @Test
        @DisplayName("EnterpriseId parameter should be request param")
        void enterpriseIdParameterShouldBeRequestParam() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            var parameters = method.getParameters();
            RequestParam requestParam = parameters[0].getAnnotation(RequestParam.class);

            // Assert
            assertNotNull(requestParam, 
                "EnterpriseId parameter should have @RequestParam annotation");
        }

        @Test
        @DisplayName("Should use request param instead of path variable")
        void shouldUseRequestParamInsteadOfPathVariable() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            var parameters = method.getParameters();

            // Assert
            assertTrue(parameters[0].isAnnotationPresent(RequestParam.class), 
                "Should use @RequestParam for enterpriseId");
            assertFalse(parameters[0].isAnnotationPresent(org.springframework.web.bind.annotation.PathVariable.class), 
                "Should not use @PathVariable");
        }
    }

    @Nested
    @DisplayName("URL Path Tests")
    class UrlPathTests {

        @Test
        @DisplayName("URL path should start with /api/kardex")
        void urlPathShouldStartWithApiKardex() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);
            String path = getExchange.value();

            // Assert
            assertTrue(path.startsWith("/api/kardex"), 
                "URL path should start with '/api/kardex'");
        }

        @Test
        @DisplayName("URL path should contain weighted-average endpoint")
        void urlPathShouldContainWeightedAverageEndpoint() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);
            String path = getExchange.value();

            // Assert
            assertTrue(path.contains("weighted-average"), 
                "URL path should contain 'weighted-average' indicating the kardex type");
        }

        @Test
        @DisplayName("URL path should indicate last kardex retrieval")
        void urlPathShouldIndicateLastKardexRetrieval() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);
            String path = getExchange.value();

            // Assert
            assertTrue(path.contains("last-kardex"), 
                "URL path should contain 'last-kardex' indicating retrieval of latest records");
        }

        @Test
        @DisplayName("URL path should indicate all products retrieval")
        void urlPathShouldIndicateAllProductsRetrieval() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);
            String path = getExchange.value();

            // Assert
            assertTrue(path.contains("all-products"), 
                "URL path should contain 'all-products' indicating retrieval for all products");
        }

        @Test
        @DisplayName("URL path should not contain path variable placeholders")
        void urlPathShouldNotContainPathVariablePlaceholders() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);
            String path = getExchange.value();

            // Assert
            assertFalse(path.contains("{"), 
                "URL path should not contain path variable placeholders");
            assertFalse(path.contains("}"), 
                "URL path should not contain path variable placeholders");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Method should retrieve last kardex for all products")
        void methodShouldRetrieveLastKardexForAllProducts() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);
            String path = getExchange.value();
            String methodName = method.getName();

            // Assert
            assertTrue(path.contains("last-kardex-all-products"), 
                "Endpoint should retrieve last kardex for all products");
            assertTrue(methodName.contains("Kardex"), 
                "Method name should indicate kardex retrieval");
        }

        @Test
        @DisplayName("Method should filter by enterprise")
        void methodShouldFilterByEnterprise() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            var parameters = method.getParameters();
            String methodName = method.getName();

            // Assert
            assertEquals(1, parameters.length, 
                "Should accept enterpriseId parameter");
            assertEquals(String.class, parameters[0].getType(), 
                "EnterpriseId should be String type");
            assertTrue(methodName.contains("ByEnterpriseId"), 
                "Method name should indicate filtering by enterprise");
        }

        @Test
        @DisplayName("Method should integrate with weighted-average kardex system")
        void methodShouldIntegrateWithWeightedAverageKardexSystem() throws Exception {
            // Arrange & Act
            Method method = IKardexExternalClient.class.getDeclaredMethod("findKardexByEnterpriseId", String.class);
            GetExchange getExchange = method.getAnnotation(GetExchange.class);
            String path = getExchange.value();

            // Assert
            assertTrue(path.contains("weighted-average"), 
                "Should integrate with weighted-average kardex system");
            assertFalse(path.contains("peps") || path.contains("PEPS"), 
                "Should call external weighted-average system, not PEPS");
        }
    }
}
