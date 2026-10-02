package co.unicauca.edu.co.contables.accounting.catalogue.unit.paymentMethods.domain.services;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.PaymentMethodUsageService;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentMethodUsageServiceUnitTest {

    @Mock
    private IPaymentMethodService paymentMethodService;

    @InjectMocks
    private PaymentMethodUsageService usageService;

    private PaymentMethod paymentMethod;
    private String enterpriseId;
    private Long paymentMethodId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        paymentMethodId = 1L;

        paymentMethod = PaymentMethod.builder()
                .id(paymentMethodId)
                .name("Efectivo")
                .accountingAccount("11050101")
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe incrementar contador de uso desde cero exitosamente")
    void testIncrementUsageCountFromZeroSuccess() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(paymentMethodId, 1);

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(paymentMethodService).findById(paymentMethodId, enterpriseId);
        verify(paymentMethodService).updateUsageCount(idCaptor.capture(), countCaptor.capture());

        assertEquals(paymentMethodId, idCaptor.getValue());
        assertEquals(1, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe incrementar contador de uso desde valor existente exitosamente")
    void testIncrementUsageCountFromExistingValueSuccess() {
        // Arrange
        paymentMethod.setUsageCount(5);
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(paymentMethodId, 6);

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(paymentMethodService).updateUsageCount(eq(paymentMethodId), countCaptor.capture());
        assertEquals(6, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando método de pago no existe")
    void testIncrementUsageCount_WithNonExistentPaymentMethod_ThrowsException() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId))
                .thenThrow(new PaymentMethodsNotFoundException());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () ->
                usageService.incrementUsageCount(paymentMethodId, enterpriseId)
        );
        verify(paymentMethodService).findById(paymentMethodId, enterpriseId);
        verify(paymentMethodService, never()).updateUsageCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException cuando findById retorna null")
    void testIncrementUsageCount_WhenFindByIdReturnsNull_ThrowsRuntimeException() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                usageService.incrementUsageCount(paymentMethodId, enterpriseId)
        );
        assertTrue(exception.getMessage().contains("Método de pago no encontrado"));
        verify(paymentMethodService).findById(paymentMethodId, enterpriseId);
        verify(paymentMethodService, never()).updateUsageCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debe incrementar contador de uso con valor grande")
    void testIncrementUsageCountWithLargeValue() {
        // Arrange
        paymentMethod.setUsageCount(999);
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(paymentMethodId, 1000);

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(paymentMethodService).updateUsageCount(eq(paymentMethodId), countCaptor.capture());
        assertEquals(1000, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe incrementar contador de uso múltiples veces consecutivas")
    void testIncrementUsageCountMultipleTimes() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId))
                .thenReturn(paymentMethod)
                .thenReturn(paymentMethod)
                .thenReturn(paymentMethod);

        doNothing().when(paymentMethodService).updateUsageCount(anyLong(), anyInt());

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);
        paymentMethod.setUsageCount(1);
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);
        paymentMethod.setUsageCount(2);
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        verify(paymentMethodService, times(3)).findById(paymentMethodId, enterpriseId);
        verify(paymentMethodService).updateUsageCount(paymentMethodId, 1);
        verify(paymentMethodService).updateUsageCount(paymentMethodId, 2);
        verify(paymentMethodService).updateUsageCount(paymentMethodId, 3);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando servicio de actualización falla")
    void testIncrementUsageCount_WhenUpdateFails_ThrowsException() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doThrow(new RuntimeException("Database error"))
                .when(paymentMethodService).updateUsageCount(paymentMethodId, 1);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                usageService.incrementUsageCount(paymentMethodId, enterpriseId)
        );
        verify(paymentMethodService).findById(paymentMethodId, enterpriseId);
        verify(paymentMethodService).updateUsageCount(paymentMethodId, 1);
    }

    @Test
    @DisplayName("Debe incrementar contador de uso para diferentes empresas")
    void testIncrementUsageCountForDifferentEnterprises() {
        // Arrange
        String enterprise1 = "ENT-001";
        String enterprise2 = "ENT-002";

        PaymentMethod method1 = PaymentMethod.builder()
                .id(paymentMethodId)
                .usageCount(3)
                .idEnterprise(enterprise1)
                .build();

        PaymentMethod method2 = PaymentMethod.builder()
                .id(paymentMethodId)
                .usageCount(5)
                .idEnterprise(enterprise2)
                .build();

        when(paymentMethodService.findById(paymentMethodId, enterprise1)).thenReturn(method1);
        when(paymentMethodService.findById(paymentMethodId, enterprise2)).thenReturn(method2);
        doNothing().when(paymentMethodService).updateUsageCount(anyLong(), anyInt());

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterprise1);
        usageService.incrementUsageCount(paymentMethodId, enterprise2);

        // Assert
        verify(paymentMethodService).updateUsageCount(paymentMethodId, 4);
        verify(paymentMethodService).updateUsageCount(paymentMethodId, 6);
    }

    @Test
    @DisplayName("Debe incrementar contador de uso para diferentes métodos de pago")
    void testIncrementUsageCountForDifferentPaymentMethods() {
        // Arrange
        Long methodId1 = 1L;
        Long methodId2 = 2L;

        PaymentMethod method1 = PaymentMethod.builder()
                .id(methodId1)
                .usageCount(0)
                .idEnterprise(enterpriseId)
                .build();

        PaymentMethod method2 = PaymentMethod.builder()
                .id(methodId2)
                .usageCount(10)
                .idEnterprise(enterpriseId)
                .build();

        when(paymentMethodService.findById(methodId1, enterpriseId)).thenReturn(method1);
        when(paymentMethodService.findById(methodId2, enterpriseId)).thenReturn(method2);
        doNothing().when(paymentMethodService).updateUsageCount(anyLong(), anyInt());

        // Act
        usageService.incrementUsageCount(methodId1, enterpriseId);
        usageService.incrementUsageCount(methodId2, enterpriseId);

        // Assert
        verify(paymentMethodService).updateUsageCount(methodId1, 1);
        verify(paymentMethodService).updateUsageCount(methodId2, 11);
    }

    @Test
    @DisplayName("Debe propagar excepción cuando findById falla con excepción genérica")
    void testIncrementUsageCount_WhenFindByIdFails_PropagatesException() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId))
                .thenThrow(new RuntimeException("Connection timeout"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                usageService.incrementUsageCount(paymentMethodId, enterpriseId)
        );
        assertEquals("Connection timeout", exception.getMessage());
        verify(paymentMethodService).findById(paymentMethodId, enterpriseId);
        verify(paymentMethodService, never()).updateUsageCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debe incrementar contador correctamente cuando método de pago está inactivo")
    void testIncrementUsageCount_WithInactivePaymentMethod_Success() {
        // Arrange
        paymentMethod.setStatus(false);
        paymentMethod.setUsageCount(2);
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(paymentMethodId, 3);

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(paymentMethodService).updateUsageCount(eq(paymentMethodId), countCaptor.capture());
        assertEquals(3, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en orden")
    void testIncrementUsageCountInvokesServicesInCorrectOrder() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(paymentMethodId, 1);

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        var inOrder = inOrder(paymentMethodService);
        inOrder.verify(paymentMethodService).findById(paymentMethodId, enterpriseId);
        inOrder.verify(paymentMethodService).updateUsageCount(paymentMethodId, 1);
    }

    @Test
    @DisplayName("Debe incrementar contador cuando usageCount inicial es null")
    void testIncrementUsageCount_WithNullUsageCount_IncrementsToOne() {
        // Arrange
        paymentMethod.setUsageCount(null);
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(paymentMethodId, 1);

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(paymentMethodService).updateUsageCount(eq(paymentMethodId), countCaptor.capture());
        assertEquals(1, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe verificar que se llama findById con parámetros correctos")
    void testIncrementUsageCount_VerifiesFindByIdParameters() {
        // Arrange
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(anyLong(), anyInt());

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> enterpriseCaptor = ArgumentCaptor.forClass(String.class);
        verify(paymentMethodService).findById(idCaptor.capture(), enterpriseCaptor.capture());
        assertEquals(paymentMethodId, idCaptor.getValue());
        assertEquals(enterpriseId, enterpriseCaptor.getValue());
    }

    @Test
    @DisplayName("Debe incrementar contador cuando método de pago tiene cuenta contable asociada")
    void testIncrementUsageCount_WithAccountingAccount_Success() {
        // Arrange
        paymentMethod.setAccountingAccount("11050102");
        paymentMethod.setUsageCount(7);
        when(paymentMethodService.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        doNothing().when(paymentMethodService).updateUsageCount(paymentMethodId, 8);

        // Act
        usageService.incrementUsageCount(paymentMethodId, enterpriseId);

        // Assert
        verify(paymentMethodService).updateUsageCount(paymentMethodId, 8);
    }
}
