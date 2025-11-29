package co.unicauca.edu.co.contables.configuration.commons.multitenancy.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.configuration.commons.multitenancy.utils.TenantContext;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TenantAwareTaskDecorator - Tests de decorador de tareas con tenant")
class TenantAwareTaskDecoratorUnitTest {

    private final TenantAwareTaskDecorator decorator = new TenantAwareTaskDecorator();

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("decorate - Preserva tenantId antes de ejecutar runnable")
    void decorate_PreservesTenantIdBeforeExecutingRunnable() {
        // Arrange
        String expectedTenantId = "tenant-123";
        TenantContext.setTenantId(expectedTenantId);
        
        String[] capturedTenantId = new String[1];
        Runnable originalTask = () -> capturedTenantId[0] = TenantContext.getTenantId();

        // Act
        Runnable decoratedTask = decorator.decorate(originalTask);
        decoratedTask.run();

        // Assert
        assertEquals(expectedTenantId, capturedTenantId[0]);
    }

    @Test
    @DisplayName("decorate - Limpia tenantId después de ejecutar runnable")
    void decorate_ClearsTenantIdAfterExecutingRunnable() {
        // Arrange
        TenantContext.setTenantId("tenant-456");
        Runnable originalTask = () -> { /* no-op */ };

        // Act
        Runnable decoratedTask = decorator.decorate(originalTask);
        decoratedTask.run();

        // Assert
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("decorate - Con tenantId null preserva null en ejecución")
    void decorate_WithNullTenantId_PreservesNullDuringExecution() {
        // Arrange
        TenantContext.setTenantId(null);
        
        String[] capturedTenantId = new String[1];
        Runnable originalTask = () -> capturedTenantId[0] = TenantContext.getTenantId();

        // Act
        Runnable decoratedTask = decorator.decorate(originalTask);
        decoratedTask.run();

        // Assert
        assertNull(capturedTenantId[0]);
    }

    @Test
    @DisplayName("decorate - Limpia tenantId incluso si runnable lanza excepción")
    void decorate_ClearsTenantIdEvenIfRunnableThrowsException() {
        // Arrange
        TenantContext.setTenantId("tenant-789");
        Runnable failingTask = () -> { throw new RuntimeException("Task failed"); };

        // Act
        Runnable decoratedTask = decorator.decorate(failingTask);
        
        // Assert
        assertThrows(RuntimeException.class, decoratedTask::run);
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("decorate - Múltiples decoraciones preservan tenantId correcto")
    void decorate_MultipleDecorations_PreserveCorrectTenantId() {
        // Arrange
        String tenant1 = "tenant-001";
        String tenant2 = "tenant-002";
        
        TenantContext.setTenantId(tenant1);
        String[] captured1 = new String[1];
        Runnable task1 = () -> captured1[0] = TenantContext.getTenantId();
        Runnable decorated1 = decorator.decorate(task1);
        
        TenantContext.setTenantId(tenant2);
        String[] captured2 = new String[1];
        Runnable task2 = () -> captured2[0] = TenantContext.getTenantId();
        Runnable decorated2 = decorator.decorate(task2);

        // Act
        decorated1.run();
        decorated2.run();

        // Assert
        assertEquals(tenant1, captured1[0]);
        assertEquals(tenant2, captured2[0]);
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("decorate - Runnable ejecuta lógica original correctamente")
    void decorate_ExecutesOriginalRunnableLogicCorrectly() {
        // Arrange
        TenantContext.setTenantId("tenant-abc");
        boolean[] executed = {false};
        Runnable originalTask = () -> executed[0] = true;

        // Act
        Runnable decoratedTask = decorator.decorate(originalTask);
        decoratedTask.run();

        // Assert
        assertTrue(executed[0]);
    }


    @Test
    @DisplayName("decorate - Con diferentes tenants en distintos threads mantiene aislamiento")
    void decorate_WithDifferentTenantsInDifferentThreads_MaintainsIsolation() throws InterruptedException {
        // Arrange
        String tenant1 = "tenant-thread1";
        String tenant2 = "tenant-thread2";
        
        String[] capturedInThread1 = new String[1];
        String[] capturedInThread2 = new String[1];
        
        TenantContext.setTenantId(tenant1);
        Runnable task1 = () -> capturedInThread1[0] = TenantContext.getTenantId();
        Runnable decorated1 = decorator.decorate(task1);
        
        TenantContext.setTenantId(tenant2);
        Runnable task2 = () -> capturedInThread2[0] = TenantContext.getTenantId();
        Runnable decorated2 = decorator.decorate(task2);

        // Act
        Thread thread1 = new Thread(decorated1);
        Thread thread2 = new Thread(decorated2);
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();

        // Assert
        assertEquals(tenant1, capturedInThread1[0]);
        assertEquals(tenant2, capturedInThread2[0]);
    }
}
