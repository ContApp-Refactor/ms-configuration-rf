package co.unicauca.edu.co.contables.configuration.unit.commons.multitenancy.interceptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.context.request.WebRequest;

import co.unicauca.edu.co.contables.configuration.commons.multitenancy.interceptor.TenantInterceptor;
import co.unicauca.edu.co.contables.configuration.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.commons.security.IJwtUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TenantInterceptor - Tests de interceptor de tenant")
class TenantInterceptorUnitTest {

    @InjectMocks
    private TenantInterceptor tenantInterceptor;

    @Mock
    private IJwtUtils jwtUtils;

    @Mock
    private WebRequest webRequest;

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    // ==================== Tests para preHandle ====================

    @Test
    @DisplayName("preHandle - Establece tenantId en contexto desde JWT")
    void preHandle_SetsTenantIdInContextFromJwt() throws Exception {
        // Arrange
        String expectedTenantId = "tenant-123";
        when(jwtUtils.getId()).thenReturn(expectedTenantId);

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertEquals(expectedTenantId, TenantContext.getTenantId());
        verify(jwtUtils, times(1)).getId();
    }

    @Test
    @DisplayName("preHandle - Establece default cuando jwtUtils falla")
    void preHandle_SetsDefaultWhenJwtUtilsFails() throws Exception {
        // Arrange
        when(jwtUtils.getId()).thenThrow(new RuntimeException("JWT error"));

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertEquals("default", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("preHandle - Con tenantId null establece default en contexto")
    void preHandle_WithNullTenantId_SetsDefaultInContext() throws Exception {
        // Arrange
        when(jwtUtils.getId()).thenReturn(null);

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertEquals("default", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("preHandle - Con diferentes tenantIds establece correctamente cada uno")
    void preHandle_WithDifferentTenantIds_SetsEachCorrectly() throws Exception {
        // Arrange
        String tenant1 = "tenant-001";
        String tenant2 = "tenant-002";

        when(jwtUtils.getId()).thenReturn(tenant1);
        tenantInterceptor.preHandle(webRequest);
        String firstTenant = TenantContext.getTenantId();

        TenantContext.clear();

        when(jwtUtils.getId()).thenReturn(tenant2);
        tenantInterceptor.preHandle(webRequest);
        String secondTenant = TenantContext.getTenantId();

        // Assert
        assertEquals(tenant1, firstTenant);
        assertEquals(tenant2, secondTenant);
    }

    // ==================== Tests para postHandle ====================

    @Test
    @DisplayName("postHandle - Limpia contexto de tenant")
    void postHandle_ClearsTenantContext() throws Exception {
        // Arrange
        TenantContext.setTenantId("tenant-456");

        // Act
        tenantInterceptor.postHandle(webRequest, null);

        // Assert
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("postHandle - No lanza excepción cuando contexto está vacío")
    void postHandle_DoesNotThrowExceptionWhenContextIsEmpty() {
        // Act & Assert
        assertDoesNotThrow(() -> tenantInterceptor.postHandle(webRequest, null));
    }

    @Test
    @DisplayName("postHandle - Puede ser llamado múltiples veces sin error")
    void postHandle_CanBeCalledMultipleTimesWithoutError() {
        // Arrange
        TenantContext.setTenantId("tenant-789");

        // Act & Assert
        assertDoesNotThrow(() -> {
            tenantInterceptor.postHandle(webRequest, null);
            tenantInterceptor.postHandle(webRequest, null);
            tenantInterceptor.postHandle(webRequest, null);
        });
    }

    // ==================== Tests para afterCompletion ====================

    @Test
    @DisplayName("afterCompletion - Completa sin errores")
    void afterCompletion_CompletesWithoutErrors() {
        // Act & Assert
        assertDoesNotThrow(() -> tenantInterceptor.afterCompletion(webRequest, null));
    }

    @Test
    @DisplayName("afterCompletion - Con excepción completa sin errores")
    void afterCompletion_WithException_CompletesWithoutErrors() {
        // Arrange
        Exception requestException = new RuntimeException("Request failed");

        // Act & Assert
        assertDoesNotThrow(() -> tenantInterceptor.afterCompletion(webRequest, requestException));
    }

    @Test
    @DisplayName("afterCompletion - No modifica contexto de tenant")
    void afterCompletion_DoesNotModifyTenantContext() throws Exception {
        // Arrange
        String expectedTenantId = "tenant-xyz";
        TenantContext.setTenantId(expectedTenantId);

        // Act
        tenantInterceptor.afterCompletion(webRequest, null);

        // Assert
        assertEquals(expectedTenantId, TenantContext.getTenantId());
    }

    // ==================== Tests de ciclo completo ====================

    @Test
    @DisplayName("Ciclo completo - preHandle, postHandle, afterCompletion funcionan correctamente")
    void fullCycle_PreHandlePostHandleAfterCompletion_WorkCorrectly() throws Exception {
        // Arrange
        String tenantId = "tenant-full-cycle";
        when(jwtUtils.getId()).thenReturn(tenantId);

        // Act & Assert
        // preHandle establece el tenant
        tenantInterceptor.preHandle(webRequest);
        assertEquals(tenantId, TenantContext.getTenantId());

        // postHandle limpia el tenant
        tenantInterceptor.postHandle(webRequest, null);
        assertNull(TenantContext.getTenantId());

        // afterCompletion no hace nada
        assertDoesNotThrow(() -> tenantInterceptor.afterCompletion(webRequest, null));
    }

    @Test
    @DisplayName("Ciclo con error - Establece default cuando JWT falla y postHandle limpia contexto")
    void cycleWithError_SetsDefaultWhenJwtFailsAndPostHandleClearsContext() throws Exception {
        // Arrange
        when(jwtUtils.getId()).thenThrow(new RuntimeException("JWT error"));

        // Act
        tenantInterceptor.preHandle(webRequest);
        assertEquals("default", TenantContext.getTenantId());
        
        tenantInterceptor.postHandle(webRequest, null);

        // Assert
        assertNull(TenantContext.getTenantId());
    }

    // ==================== Tests de casos edge ====================

    @Test
    @DisplayName("preHandle - Con string vacío como tenantId establece default")
    void preHandle_WithEmptyStringAsTenantId_SetsDefault() throws Exception {
        // Arrange
        when(jwtUtils.getId()).thenReturn("");

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertEquals("default", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("preHandle - Con espacio en blanco como tenantId establece default")
    void preHandle_WithWhitespaceAsTenantId_SetsDefault() throws Exception {
        // Arrange
        when(jwtUtils.getId()).thenReturn("   ");

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertEquals("default", TenantContext.getTenantId());
    }
}
