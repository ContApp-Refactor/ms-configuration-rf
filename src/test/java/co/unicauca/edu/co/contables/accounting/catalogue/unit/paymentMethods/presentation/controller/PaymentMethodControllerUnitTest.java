package co.unicauca.edu.co.contables.accounting.catalogue.unit.paymentMethods.presentation.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.mapper.PaymentMethodDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.response.PaymentMethodRes;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.controller.PaymentMethodController;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentMethodControllerUnitTest {

    @Mock
    private IPaymentMethodService service;

    @Mock
    private PaymentMethodDomainMapper mapper;

    @InjectMocks
    private PaymentMethodController controller;

    private PaymentMethod paymentMethod;
    private PaymentMethodCreateReq createRequest;
    private PaymentMethodUpdateReq updateRequest;
    private PaymentMethodRes paymentMethodRes;
    private String enterpriseId;
    private Long paymentMethodId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        paymentMethodId = 1L;

        paymentMethod = PaymentMethod.builder()
                .id(paymentMethodId)
                .name("EFECTIVO")
                .accountingAccount("11050101")
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();

        createRequest = PaymentMethodCreateReq.builder()
                .idEnterprise(enterpriseId)
                .name("Efectivo")
                .accountingAccountId(1L)
                .status(true)
                .build();

        updateRequest = PaymentMethodUpdateReq.builder()
                .id(paymentMethodId)
                .idEnterprise(enterpriseId)
                .name("Efectivo Actualizado")
                .accountingAccountId(1L)
                .build();

        paymentMethodRes = PaymentMethodRes.builder()
                .id(paymentMethodId)
                .name("EFECTIVO")
                .accountingAccount("11050101 - Caja General")
                .accountingAccountId(1L)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe crear método de pago exitosamente")
    void testCreateSuccess() {
        // Arrange
        when(service.create(createRequest)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(paymentMethodId, result.getBody().getId());
        assertEquals("EFECTIVO", result.getBody().getName());
        verify(service).create(createRequest);
        verify(mapper).toRes(paymentMethod);
    }

    @Test
    @DisplayName("Debe actualizar método de pago exitosamente")
    void testUpdateSuccess() {
        // Arrange
        when(service.update(updateRequest)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(paymentMethodId, result.getBody().getId());
        verify(service).update(updateRequest);
        verify(mapper).toRes(paymentMethod);
    }

    @Test
    @DisplayName("Debe obtener método de pago por ID exitosamente")
    void testGetByIdSuccess() {
        // Arrange
        when(service.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.getById(paymentMethodId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(paymentMethodId, result.getBody().getId());
        assertEquals(enterpriseId, result.getBody().getIdEnterprise());
        verify(service).findById(paymentMethodId, enterpriseId);
        verify(mapper).toRes(paymentMethod);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener método de pago inexistente")
    void testGetById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(service.findById(paymentMethodId, enterpriseId))
                .thenThrow(new PaymentMethodsNotFoundException());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () ->
                controller.getById(paymentMethodId, enterpriseId)
        );
        verify(service).findById(paymentMethodId, enterpriseId);
        verify(mapper, never()).toRes(any());
    }

    @Test
    @DisplayName("Debe listar métodos de pago con filtros exitosamente")
    void testListWithFiltersSuccess() {
        // Arrange
        List<PaymentMethod> methods = List.of(paymentMethod);
        Page<PaymentMethod> methodsPage = new PageImpl<>(methods);

        when(service.findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), isNull()))
                .thenReturn(methodsPage);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.list(
                enterpriseId, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
        verify(service).findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), isNull());
    }

    @Test
    @DisplayName("Debe listar métodos de pago con búsqueda")
    void testListWithSearchSuccess() {
        // Arrange
        String searchTerm = "Efectivo";
        List<PaymentMethod> methods = List.of(paymentMethod);
        Page<PaymentMethod> methodsPage = new PageImpl<>(methods);

        when(service.findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), eq(searchTerm)))
                .thenReturn(methodsPage);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.list(
                enterpriseId, Optional.of(0), Optional.of(10), "name", "asc", searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(service).findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), eq(searchTerm));
    }

    @Test
    @DisplayName("Debe listar métodos de pago con parámetros opcionales vacíos")
    void testList_WithEmptyOptionals_Success() {
        // Arrange
        List<PaymentMethod> methods = List.of(paymentMethod);
        Page<PaymentMethod> methodsPage = new PageImpl<>(methods);

        when(service.findAllByEnterprise(eq(enterpriseId), eq(Optional.empty()), eq(Optional.empty()), 
                eq("name"), eq("asc"), isNull()))
                .thenReturn(methodsPage);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.list(
                enterpriseId, Optional.empty(), Optional.empty(), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterprise(eq(enterpriseId), eq(Optional.empty()), eq(Optional.empty()), 
                eq("name"), eq("asc"), isNull());
    }

    @Test
    @DisplayName("Debe listar métodos de pago activos exitosamente")
    void testFindAllActiveSuccess() {
        // Arrange
        List<PaymentMethod> methods = List.of(paymentMethod);
        Page<PaymentMethod> methodsPage = new PageImpl<>(methods);

        when(service.findAllActiveByEnterprise(enterpriseId, Optional.of(0), Optional.of(10)))
                .thenReturn(methodsPage);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.findAllActive(
                enterpriseId, Optional.of(0), Optional.of(10));

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        assertTrue(result.getBody().getContent().get(0).getStatus());
        verify(service).findAllActiveByEnterprise(enterpriseId, Optional.of(0), Optional.of(10));
    }

    @Test
    @DisplayName("Debe listar métodos de pago activos con parámetros opcionales vacíos")
    void testFindAllActive_WithEmptyOptionals_Success() {
        // Arrange
        List<PaymentMethod> methods = List.of(paymentMethod);
        Page<PaymentMethod> methodsPage = new PageImpl<>(methods);

        when(service.findAllActiveByEnterprise(enterpriseId, Optional.empty(), Optional.empty()))
                .thenReturn(methodsPage);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.findAllActive(
                enterpriseId, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllActiveByEnterprise(enterpriseId, Optional.empty(), Optional.empty());
    }

    @Test
    @DisplayName("Debe cambiar estado de método de pago a inactivo exitosamente")
    void testChangeStateToInactiveSuccess() {
        // Arrange
        paymentMethod.setStatus(false);
        paymentMethodRes.setStatus(false);

        when(service.changeState(paymentMethodId, enterpriseId, false)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.changeState(paymentMethodId, enterpriseId, false);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().getStatus());
        verify(service).changeState(paymentMethodId, enterpriseId, false);
        verify(mapper).toRes(paymentMethod);
    }

    @Test
    @DisplayName("Debe cambiar estado de método de pago a activo exitosamente")
    void testChangeStateToActiveSuccess() {
        // Arrange
        when(service.changeState(paymentMethodId, enterpriseId, true)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.changeState(paymentMethodId, enterpriseId, true);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().getStatus());
        verify(service).changeState(paymentMethodId, enterpriseId, true);
        verify(mapper).toRes(paymentMethod);
    }

    @Test
    @DisplayName("Debe eliminar método de pago exitosamente")
    void testDeleteSuccess() {
        // Arrange
        when(service.delete(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.delete(paymentMethodId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(paymentMethodId, result.getBody().getId());
        verify(service).delete(paymentMethodId, enterpriseId);
        verify(mapper).toRes(paymentMethod);
    }

    @Test
    @DisplayName("Debe propagar excepción al eliminar método de pago inexistente")
    void testDelete_WithNonExistentPaymentMethod_ThrowsException() {
        // Arrange
        when(service.delete(paymentMethodId, enterpriseId))
                .thenThrow(new PaymentMethodsNotFoundException());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () ->
                controller.delete(paymentMethodId, enterpriseId)
        );
        verify(service).delete(paymentMethodId, enterpriseId);
        verify(mapper, never()).toRes(any());
    }

    @Test
    @DisplayName("Debe actualizar nombre de método de pago")
    void testUpdate_ChangingName_Success() {
        // Arrange
        updateRequest.setName("Transferencia Bancaria");
        paymentMethod.setName("TRANSFERENCIA BANCARIA");
        paymentMethodRes.setName("TRANSFERENCIA BANCARIA");

        when(service.update(updateRequest)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("TRANSFERENCIA BANCARIA", result.getBody().getName());
        verify(service).update(updateRequest);
    }

    @Test
    @DisplayName("Debe listar métodos de pago con ordenamiento descendente")
    void testList_WithDescOrder_Success() {
        // Arrange
        List<PaymentMethod> methods = List.of(paymentMethod);
        Page<PaymentMethod> methodsPage = new PageImpl<>(methods);

        when(service.findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("desc"), isNull()))
                .thenReturn(methodsPage);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.list(
                enterpriseId, Optional.of(0), Optional.of(10), "name", "desc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("desc"), isNull());
    }

    @Test
    @DisplayName("Debe obtener método de pago de diferentes empresas")
    void testGetById_DifferentEnterprises_Success() {
        // Arrange
        String enterprise2 = "ENT-002";
        PaymentMethod method2 = PaymentMethod.builder()
                .id(paymentMethodId)
                .idEnterprise(enterprise2)
                .build();
        PaymentMethodRes res2 = PaymentMethodRes.builder()
                .id(paymentMethodId)
                .idEnterprise(enterprise2)
                .build();

        when(service.findById(paymentMethodId, enterprise2)).thenReturn(method2);
        when(mapper.toRes(method2)).thenReturn(res2);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.getById(paymentMethodId, enterprise2);

        // Assert
        assertNotNull(result);
        assertEquals(enterprise2, result.getBody().getIdEnterprise());
        verify(service).findById(paymentMethodId, enterprise2);
    }

    @Test
    @DisplayName("Debe crear múltiples métodos de pago")
    void testCreate_MultiplePaymentMethods_Success() {
        // Arrange
        PaymentMethodCreateReq request2 = PaymentMethodCreateReq.builder()
                .idEnterprise(enterpriseId)
                .name("Tarjeta de Crédito")
                .accountingAccountId(2L)
                .build();

        PaymentMethod method2 = PaymentMethod.builder()
                .id(2L)
                .name("TARJETA DE CRÉDITO")
                .build();

        PaymentMethodRes res2 = PaymentMethodRes.builder()
                .id(2L)
                .name("TARJETA DE CRÉDITO")
                .build();

        when(service.create(createRequest)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);
        when(service.create(request2)).thenReturn(method2);
        when(mapper.toRes(method2)).thenReturn(res2);

        // Act
        ResponseEntity<PaymentMethodRes> result1 = controller.create(createRequest);
        ResponseEntity<PaymentMethodRes> result2 = controller.create(request2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1L, result1.getBody().getId());
        assertEquals(2L, result2.getBody().getId());
        verify(service, times(1)).create(createRequest);
        verify(service, times(1)).create(request2);
    }

    @Test
    @DisplayName("Debe listar métodos de pago vacíos")
    void testList_EmptyResults_Success() {
        // Arrange
        Page<PaymentMethod> emptyPage = Page.empty();

        when(service.findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), isNull()))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.list(
                enterpriseId, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
        verify(service).findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), isNull());
    }

    @Test
    @DisplayName("Debe mapear correctamente la respuesta en todos los endpoints")
    void testMapperInvocation_InAllEndpoints() {
        // Arrange
        when(service.create(createRequest)).thenReturn(paymentMethod);
        when(service.update(updateRequest)).thenReturn(paymentMethod);
        when(service.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        when(service.changeState(paymentMethodId, enterpriseId, false)).thenReturn(paymentMethod);
        when(service.delete(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        controller.create(createRequest);
        controller.update(updateRequest);
        controller.getById(paymentMethodId, enterpriseId);
        controller.changeState(paymentMethodId, enterpriseId, false);
        controller.delete(paymentMethodId, enterpriseId);

        // Assert
        verify(mapper, times(5)).toRes(paymentMethod);
    }

    @Test
    @DisplayName("Debe retornar OK en todos los endpoints exitosos")
    void testHttpStatus_AllEndpoints_ReturnOK() {
        // Arrange
        Page<PaymentMethod> methodsPage = new PageImpl<>(List.of(paymentMethod));

        when(service.create(createRequest)).thenReturn(paymentMethod);
        when(service.update(updateRequest)).thenReturn(paymentMethod);
        when(service.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        when(service.findAllByEnterprise(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(methodsPage);
        when(service.findAllActiveByEnterprise(anyString(), any(), any())).thenReturn(methodsPage);
        when(service.changeState(paymentMethodId, enterpriseId, false)).thenReturn(paymentMethod);
        when(service.delete(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> createResult = controller.create(createRequest);
        ResponseEntity<PaymentMethodRes> updateResult = controller.update(updateRequest);
        ResponseEntity<PaymentMethodRes> getByIdResult = controller.getById(paymentMethodId, enterpriseId);
        ResponseEntity<Page<PaymentMethodRes>> listResult = controller.list(
                enterpriseId, Optional.of(0), Optional.of(10), "name", "asc", null);
        ResponseEntity<Page<PaymentMethodRes>> listActiveResult = controller.findAllActive(
                enterpriseId, Optional.of(0), Optional.of(10));
        ResponseEntity<PaymentMethodRes> changeStateResult = controller.changeState(
                paymentMethodId, enterpriseId, false);
        ResponseEntity<PaymentMethodRes> deleteResult = controller.delete(paymentMethodId, enterpriseId);

        // Assert
        assertEquals(HttpStatus.OK, createResult.getStatusCode());
        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        assertEquals(HttpStatus.OK, getByIdResult.getStatusCode());
        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertEquals(HttpStatus.OK, listActiveResult.getStatusCode());
        assertEquals(HttpStatus.OK, changeStateResult.getStatusCode());
        assertEquals(HttpStatus.OK, deleteResult.getStatusCode());
    }

    @Test
    @DisplayName("Debe verificar cuenta contable en la respuesta")
    void testCreate_VerifyAccountingAccountInResponse() {
        // Arrange
        when(service.create(createRequest)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result.getBody().getAccountingAccount());
        assertEquals("11050101 - Caja General", result.getBody().getAccountingAccount());
        assertEquals(1L, result.getBody().getAccountingAccountId());
    }

    @Test
    @DisplayName("Debe verificar usageCount en la respuesta")
    void testGetById_VerifyUsageCountInResponse() {
        // Arrange
        paymentMethod.setUsageCount(5);
        paymentMethodRes.setUsageCount(5);

        when(service.findById(paymentMethodId, enterpriseId)).thenReturn(paymentMethod);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<PaymentMethodRes> result = controller.getById(paymentMethodId, enterpriseId);

        // Assert
        assertNotNull(result.getBody().getUsageCount());
        assertEquals(5, result.getBody().getUsageCount());
    }

    @Test
    @DisplayName("Debe buscar por cuenta contable")
    void testList_SearchByAccountingAccount_Success() {
        // Arrange
        String searchTerm = "11050101";
        List<PaymentMethod> methods = List.of(paymentMethod);
        Page<PaymentMethod> methodsPage = new PageImpl<>(methods);

        when(service.findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), eq(searchTerm)))
                .thenReturn(methodsPage);
        when(mapper.toRes(paymentMethod)).thenReturn(paymentMethodRes);

        // Act
        ResponseEntity<Page<PaymentMethodRes>> result = controller.list(
                enterpriseId, Optional.of(0), Optional.of(10), "name", "asc", searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBody().getTotalElements());
        verify(service).findAllByEnterprise(eq(enterpriseId), eq(Optional.of(0)), eq(Optional.of(10)), 
                eq("name"), eq("asc"), eq(searchTerm));
    }

    @Test
    @DisplayName("Debe cambiar estado de método de pago inexistente y lanzar excepción")
    void testChangeState_WithNonExistentPaymentMethod_ThrowsException() {
        // Arrange
        when(service.changeState(paymentMethodId, enterpriseId, true))
                .thenThrow(new PaymentMethodsNotFoundException());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () ->
                controller.changeState(paymentMethodId, enterpriseId, true)
        );
        verify(service).changeState(paymentMethodId, enterpriseId, true);
        verify(mapper, never()).toRes(any());
    }
}
