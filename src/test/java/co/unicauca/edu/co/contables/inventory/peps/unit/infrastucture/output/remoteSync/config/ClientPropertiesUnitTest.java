package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config.ClientProperties;

/**
 * @brief Unit tests for ClientProperties
 *
 * Desde la migración de inventory-peps-rf al monolito, {@code services} solo
 * mantiene {@code kardex-external}: stock, products y config son llamadas
 * in-process y ya no piden base-url. Estos tests cubren ese contrato.
 */
@DisplayName("ClientProperties Tests")
class ClientPropertiesUnitTest {

    private ClientProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ClientProperties();
    }

    @Test
    @DisplayName("Should default kardex-external to an empty configuration object")
    void testDefaultInitialization() {
        assertNotNull(properties.getKardexExternal());
        assertNull(properties.getKardexExternal().getBaseUrl());
    }

    @Test
    @DisplayName("Should set and get kardex-external base URL")
    void testSetAndGetKardexExternalBaseUrl() {
        properties.getKardexExternal().setBaseUrl("http://inventory-weighted-average-rf:8080");

        assertEquals("http://inventory-weighted-average-rf:8080", properties.getKardexExternal().getBaseUrl());
        assertDoesNotThrow(properties::validate);
    }

    @Test
    @DisplayName("Should validate successfully with a configured URL")
    void testValidate_WithValidUrl() {
        properties.getKardexExternal().setBaseUrl("http://localhost:8080");
        assertDoesNotThrow(properties::validate);
    }

    @Test
    @DisplayName("Should throw when kardex-external base URL is null")
    void testValidate_WithNullUrl() {
        properties.getKardexExternal().setBaseUrl(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertEquals("Kardex External base URL not configured. Expected 'services.kardex-external.base-url'", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw when kardex-external base URL is empty")
    void testValidate_WithEmptyUrl() {
        properties.getKardexExternal().setBaseUrl("");

        IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
        assertEquals("Kardex External base URL not configured. Expected 'services.kardex-external.base-url'", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw when kardex-external base URL is blank")
    void testValidate_WithBlankUrl() {
        properties.getKardexExternal().setBaseUrl("   ");

        assertThrows(IllegalStateException.class, properties::validate);
    }

    @Test
    @DisplayName("Should no longer expose the stock/products/config URLs removed by the migration")
    void testRemovedServiceUrlsAreGone() {
        assertNull(readAbsentProperty(properties, "getStock"));
        assertNull(readAbsentProperty(properties, "getProducts"));
        assertNull(readAbsentProperty(properties, "getConfig"));
    }

    private Object readAbsentProperty(ClientProperties target, String method) {
        try {
            return ClientProperties.class.getMethod(method).invoke(target);
        } catch (NoSuchMethodException expected) {
            // El método ya no existe: es exactamente el contrato nuevo.
            return null;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unexpected reflective failure", e);
        }
    }
}
