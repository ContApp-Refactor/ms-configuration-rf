package co.unicauca.edu.co.contables.configuration.commons.multitenancy.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TenantContext - Tests de contexto de tenant thread-local")
class TenantContextUnitTest {

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    // ==================== Tests para setTenantId y getTenantId ====================

    @Test
    @DisplayName("setTenantId y getTenantId - Guarda y recupera tenantId correctamente")
    void setTenantIdAndGetTenantId_StoresAndRetrievesTenantIdCorrectly() {
        // Arrange
        String expectedTenantId = "tenant-123";

        // Act
        TenantContext.setTenantId(expectedTenantId);
        String actualTenantId = TenantContext.getTenantId();

        // Assert
        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    @DisplayName("setTenantId - Sobrescribe tenantId anterior")
    void setTenantId_OverridesPreviousTenantId() {
        // Arrange
        TenantContext.setTenantId("tenant-old");

        // Act
        String newTenantId = "tenant-new";
        TenantContext.setTenantId(newTenantId);
        String actualTenantId = TenantContext.getTenantId();

        // Assert
        assertEquals(newTenantId, actualTenantId);
    }

    @Test
    @DisplayName("setTenantId - Acepta null como valor")
    void setTenantId_AcceptsNullValue() {
        // Arrange
        TenantContext.setTenantId("tenant-123");

        // Act
        TenantContext.setTenantId(null);
        String actualTenantId = TenantContext.getTenantId();

        // Assert
        assertNull(actualTenantId);
    }

    @Test
    @DisplayName("getTenantId - Retorna null cuando no se ha establecido tenantId")
    void getTenantId_ReturnsNullWhenNoTenantIdSet() {
        // Act
        String tenantId = TenantContext.getTenantId();

        // Assert
        assertNull(tenantId);
    }

    // ==================== Tests para clear ====================

    @Test
    @DisplayName("clear - Elimina tenantId del contexto")
    void clear_RemovesTenantIdFromContext() {
        // Arrange
        TenantContext.setTenantId("tenant-456");

        // Act
        TenantContext.clear();
        String tenantId = TenantContext.getTenantId();

        // Assert
        assertNull(tenantId);
    }

    @Test
    @DisplayName("clear - No lanza excepción cuando no hay tenantId")
    void clear_DoesNotThrowExceptionWhenNoTenantId() {
        // Act & Assert
        assertDoesNotThrow(() -> TenantContext.clear());
    }

    @Test
    @DisplayName("clear - Permite establecer nuevo tenantId después de limpiar")
    void clear_AllowsSettingNewTenantIdAfterClear() {
        // Arrange
        TenantContext.setTenantId("tenant-old");
        TenantContext.clear();

        // Act
        String newTenantId = "tenant-new";
        TenantContext.setTenantId(newTenantId);
        String actualTenantId = TenantContext.getTenantId();

        // Assert
        assertEquals(newTenantId, actualTenantId);
    }

    // ==================== Tests de aislamiento entre threads ====================

    @Test
    @DisplayName("Múltiples threads - Mantienen tenantId aislado por thread")
    void multipleThreads_MaintainIsolatedTenantIdPerThread() throws InterruptedException {
        // Arrange
        String tenant1 = "tenant-thread1";
        String tenant2 = "tenant-thread2";
        String[] capturedTenant1 = new String[1];
        String[] capturedTenant2 = new String[1];

        // Act
        Thread thread1 = new Thread(() -> {
            TenantContext.setTenantId(tenant1);
            capturedTenant1[0] = TenantContext.getTenantId();
            TenantContext.clear();
        });

        Thread thread2 = new Thread(() -> {
            TenantContext.setTenantId(tenant2);
            capturedTenant2[0] = TenantContext.getTenantId();
            TenantContext.clear();
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Assert
        assertEquals(tenant1, capturedTenant1[0]);
        assertEquals(tenant2, capturedTenant2[0]);
    }

    @Test
    @DisplayName("Thread hijo - Hereda tenantId del thread padre")
    void childThread_InheritsTenantIdFromParentThread() throws InterruptedException {
        // Arrange
        String parentTenantId = "parent-tenant";
        TenantContext.setTenantId(parentTenantId);
        String[] childTenantId = new String[1];

        // Act
        Thread childThread = new Thread(() -> {
            childTenantId[0] = TenantContext.getTenantId();
        });

        childThread.start();
        childThread.join();

        // Assert
        assertEquals(parentTenantId, childTenantId[0]);
    }

    @Test
    @DisplayName("Thread hijo - Modificación no afecta thread padre")
    void childThread_ModificationDoesNotAffectParentThread() throws InterruptedException {
        // Arrange
        String parentTenantId = "parent-tenant";
        TenantContext.setTenantId(parentTenantId);

        // Act
        Thread childThread = new Thread(() -> {
            TenantContext.setTenantId("child-tenant");
        });

        childThread.start();
        childThread.join();

        String parentTenantAfterChild = TenantContext.getTenantId();

        // Assert
        assertEquals(parentTenantId, parentTenantAfterChild);
    }

    // ==================== Tests de casos edge ====================

    @Test
    @DisplayName("setTenantId - Con string vacío guarda correctamente")
    void setTenantId_WithEmptyString_StoresCorrectly() {
        // Arrange
        String emptyTenantId = "";

        // Act
        TenantContext.setTenantId(emptyTenantId);
        String actualTenantId = TenantContext.getTenantId();

        // Assert
        assertEquals(emptyTenantId, actualTenantId);
    }

    @Test
    @DisplayName("setTenantId - Con string con espacios guarda correctamente")
    void setTenantId_WithWhitespaceString_StoresCorrectly() {
        // Arrange
        String whitespaceTenantId = "  tenant-with-spaces  ";

        // Act
        TenantContext.setTenantId(whitespaceTenantId);
        String actualTenantId = TenantContext.getTenantId();

        // Assert
        assertEquals(whitespaceTenantId, actualTenantId);
    }

    @Test
    @DisplayName("Operaciones sucesivas - Set, get, clear, get mantienen consistencia")
    void successiveOperations_SetGetClearGet_MaintainConsistency() {
        // Arrange
        String tenantId = "tenant-xyz";

        // Act & Assert
        TenantContext.setTenantId(tenantId);
        assertEquals(tenantId, TenantContext.getTenantId());

        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
    }
}
