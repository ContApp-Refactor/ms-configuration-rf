package co.unicauca.edu.co.contables.accounting.catalogue.unit.bankAccounts.presentation.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.mapper.BankAccountDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.response.BankAccountRes;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.controller.BankAccountController;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.response.BankRes;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts.BankAccountNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankAccountControllerUnitTest {

    @Mock
    private IBankAccountService service;

    @Mock
    private BankAccountDomainMapper mapper;

    @InjectMocks
    private BankAccountController controller;

    private BankAccount bankAccount;
    private BankAccountCreateReq createRequest;
    private BankAccountUpdateReq updateRequest;
    private BankAccountRes bankAccountRes;
    private String enterpriseId;
    private Long bankAccountId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        bankAccountId = 1L;

        Bank bank = Bank.builder()
                .id(1L)
                .code("001")
                .name("Banco Test")
                .status(true)
                .idEnterprise(enterpriseId)
                .build();

        bankAccount = BankAccount.builder()
                .id(bankAccountId)
                .accountNumber(12345678L)
                .bank(bank)
                .accountType(AccountType.AHORROS)
                .accountingAccountId(1L)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();

        createRequest = BankAccountCreateReq.builder()
                .idEnterprise(enterpriseId)
                .accountNumber(12345678L)
                .bankId(1L)
                .accountType(AccountType.AHORROS)
                .accountingAccountId(1L)
                .status(true)
                .build();

        updateRequest = BankAccountUpdateReq.builder()
                .id(bankAccountId)
                .idEnterprise(enterpriseId)
                .accountNumber(12345678L)
                .bankId(1L)
                .accountType(AccountType.AHORROS)
                .accountingAccountId(1L)
                .status(true)
                .build();

        BankRes bankRes = BankRes.builder()
                .id(1L)
                .code("001")
                .name("Banco Test")
                .status(true)
                .idEnterprise(enterpriseId)
                .build();

        bankAccountRes = BankAccountRes.builder()
                .id(bankAccountId)
                .accountNumber(12345678L)
                .bank(bankRes)
                .accountType(AccountType.AHORROS)
                .accountingAccountId(1L)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe crear cuenta bancaria exitosamente")
    void testCreateSuccess() {
        // Arrange
        when(service.create(createRequest)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankAccountId, result.getBody().getId());
        assertEquals(12345678L, result.getBody().getAccountNumber());
        verify(service).create(createRequest);
        verify(mapper).toRes(bankAccount);
    }

    @Test
    @DisplayName("Debe actualizar cuenta bancaria exitosamente")
    void testUpdateSuccess() {
        // Arrange
        when(service.update(updateRequest)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankAccountId, result.getBody().getId());
        verify(service).update(updateRequest);
        verify(mapper).toRes(bankAccount);
    }

    @Test
    @DisplayName("Debe obtener cuenta bancaria por ID exitosamente")
    void testGetByIdSuccess() {
        // Arrange
        when(service.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.getById(bankAccountId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankAccountId, result.getBody().getId());
        assertEquals(enterpriseId, result.getBody().getIdEnterprise());
        verify(service).findById(bankAccountId, enterpriseId);
        verify(mapper).toRes(bankAccount);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener cuenta bancaria inexistente")
    void testGetById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(service.findById(bankAccountId, enterpriseId))
                .thenThrow(new BankAccountNotFoundException());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () ->
                controller.getById(bankAccountId, enterpriseId)
        );
        verify(service).findById(bankAccountId, enterpriseId);
        verify(mapper, never()).toRes(any());
    }

    @Test
    @DisplayName("Debe listar cuentas bancarias con filtros exitosamente")
    void testListWithFiltersSuccess() {
        // Arrange
        List<BankAccount> accounts = List.of(bankAccount);
        Page<BankAccount> accountsPage = new PageImpl<>(accounts);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "asc", null))
                .thenReturn(accountsPage);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<Page<BankAccountRes>> result = controller.list(
                enterpriseId, 0, 10, "accountNumber", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "asc", null);
    }

    @Test
    @DisplayName("Debe listar cuentas bancarias con búsqueda")
    void testListWithSearchSuccess() {
        // Arrange
        String searchTerm = "12345";
        List<BankAccount> accounts = List.of(bankAccount);
        Page<BankAccount> accountsPage = new PageImpl<>(accounts);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "asc", searchTerm))
                .thenReturn(accountsPage);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<Page<BankAccountRes>> result = controller.list(
                enterpriseId, 0, 10, "accountNumber", "asc", searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "asc", searchTerm);
    }

    @Test
    @DisplayName("Debe listar cuentas bancarias con parámetros null")
    void testList_WithNullParams_Success() {
        // Arrange
        List<BankAccount> accounts = List.of(bankAccount);
        Page<BankAccount> accountsPage = new PageImpl<>(accounts);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, null, null, null, null, null))
                .thenReturn(accountsPage);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<Page<BankAccountRes>> result = controller.list(
                enterpriseId, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, null, null, null, null, null);
    }

    @Test
    @DisplayName("Debe listar cuentas bancarias activas exitosamente")
    void testListActiveSuccess() {
        // Arrange
        List<BankAccount> accounts = List.of(bankAccount);
        Page<BankAccount> accountsPage = new PageImpl<>(accounts);

        when(service.findAllActiveByEnterprise(enterpriseId, 0, 10)).thenReturn(accountsPage);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<Page<BankAccountRes>> result = controller.listActive(enterpriseId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        assertTrue(result.getBody().getContent().get(0).getStatus());
        verify(service).findAllActiveByEnterprise(enterpriseId, 0, 10);
    }

    @Test
    @DisplayName("Debe listar cuentas activas con parámetros null")
    void testListActive_WithNullParams_Success() {
        // Arrange
        List<BankAccount> accounts = List.of(bankAccount);
        Page<BankAccount> accountsPage = new PageImpl<>(accounts);

        when(service.findAllActiveByEnterprise(enterpriseId, null, null)).thenReturn(accountsPage);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<Page<BankAccountRes>> result = controller.listActive(enterpriseId, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllActiveByEnterprise(enterpriseId, null, null);
    }

    @Test
    @DisplayName("Debe cambiar estado de cuenta bancaria a inactivo exitosamente")
    void testChangeStateToInactiveSuccess() {
        // Arrange
        bankAccount.setStatus(false);
        bankAccountRes.setStatus(false);

        when(service.changeState(bankAccountId, enterpriseId, false)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.changeState(bankAccountId, enterpriseId, false);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().getStatus());
        verify(service).changeState(bankAccountId, enterpriseId, false);
        verify(mapper).toRes(bankAccount);
    }

    @Test
    @DisplayName("Debe cambiar estado de cuenta bancaria a activo exitosamente")
    void testChangeStateToActiveSuccess() {
        // Arrange
        when(service.changeState(bankAccountId, enterpriseId, true)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.changeState(bankAccountId, enterpriseId, true);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().getStatus());
        verify(service).changeState(bankAccountId, enterpriseId, true);
        verify(mapper).toRes(bankAccount);
    }

    @Test
    @DisplayName("Debe eliminar cuenta bancaria exitosamente")
    void testDeleteSuccess() {
        // Arrange
        when(service.delete(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.delete(bankAccountId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankAccountId, result.getBody().getId());
        verify(service).delete(bankAccountId, enterpriseId);
        verify(mapper).toRes(bankAccount);
    }

    @Test
    @DisplayName("Debe propagar excepción al eliminar cuenta inexistente")
    void testDelete_WithNonExistentAccount_ThrowsException() {
        // Arrange
        when(service.delete(bankAccountId, enterpriseId))
                .thenThrow(new BankAccountNotFoundException());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () ->
                controller.delete(bankAccountId, enterpriseId)
        );
        verify(service).delete(bankAccountId, enterpriseId);
        verify(mapper, never()).toRes(any());
    }

    @Test
    @DisplayName("Debe crear cuenta bancaria con tipo CORRIENTE")
    void testCreate_WithCorrienteType_Success() {
        // Arrange
        createRequest.setAccountType(AccountType.CORRIENTE);
        bankAccount.setAccountType(AccountType.CORRIENTE);
        bankAccountRes.setAccountType(AccountType.CORRIENTE);

        when(service.create(createRequest)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(AccountType.CORRIENTE, result.getBody().getAccountType());
        verify(service).create(createRequest);
    }

    @Test
    @DisplayName("Debe actualizar número de cuenta bancaria")
    void testUpdate_ChangingAccountNumber_Success() {
        // Arrange
        updateRequest.setAccountNumber(87654321L);
        bankAccount.setAccountNumber(87654321L);
        bankAccountRes.setAccountNumber(87654321L);

        when(service.update(updateRequest)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<BankAccountRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(87654321L, result.getBody().getAccountNumber());
        verify(service).update(updateRequest);
    }

    @Test
    @DisplayName("Debe listar cuentas con ordenamiento descendente")
    void testList_WithDescOrder_Success() {
        // Arrange
        List<BankAccount> accounts = List.of(bankAccount);
        Page<BankAccount> accountsPage = new PageImpl<>(accounts);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "desc", null))
                .thenReturn(accountsPage);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        ResponseEntity<Page<BankAccountRes>> result = controller.list(
                enterpriseId, 0, 10, "accountNumber", "desc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "desc", null);
    }

    @Test
    @DisplayName("Debe obtener cuenta bancaria de diferentes empresas")
    void testGetById_DifferentEnterprises_Success() {
        // Arrange
        String enterprise2 = "ENT-002";
        BankAccount account2 = BankAccount.builder()
                .id(bankAccountId)
                .idEnterprise(enterprise2)
                .build();
        BankAccountRes res2 = BankAccountRes.builder()
                .id(bankAccountId)
                .idEnterprise(enterprise2)
                .build();

        when(service.findById(bankAccountId, enterprise2)).thenReturn(account2);
        when(mapper.toRes(account2)).thenReturn(res2);

        // Act
        ResponseEntity<BankAccountRes> result = controller.getById(bankAccountId, enterprise2);

        // Assert
        assertNotNull(result);
        assertEquals(enterprise2, result.getBody().getIdEnterprise());
        verify(service).findById(bankAccountId, enterprise2);
    }

    @Test
    @DisplayName("Debe crear múltiples cuentas bancarias")
    void testCreate_MultipleAccounts_Success() {
        // Arrange
        BankAccountCreateReq request2 = BankAccountCreateReq.builder()
                .idEnterprise(enterpriseId)
                .accountNumber(87654321L)
                .bankId(1L)
                .accountType(AccountType.CORRIENTE)
                .accountingAccountId(1L)
                .build();

        BankAccount account2 = BankAccount.builder()
                .id(2L)
                .accountNumber(87654321L)
                .accountType(AccountType.CORRIENTE)
                .build();

        BankAccountRes res2 = BankAccountRes.builder()
                .id(2L)
                .accountNumber(87654321L)
                .accountType(AccountType.CORRIENTE)
                .build();

        when(service.create(createRequest)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);
        when(service.create(request2)).thenReturn(account2);
        when(mapper.toRes(account2)).thenReturn(res2);

        // Act
        ResponseEntity<BankAccountRes> result1 = controller.create(createRequest);
        ResponseEntity<BankAccountRes> result2 = controller.create(request2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1L, result1.getBody().getId());
        assertEquals(2L, result2.getBody().getId());
        verify(service, times(1)).create(createRequest);
        verify(service, times(1)).create(request2);
    }

    @Test
    @DisplayName("Debe listar cuentas bancarias vacías")
    void testList_EmptyResults_Success() {
        // Arrange
        Page<BankAccount> emptyPage = Page.empty();

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "asc", null))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<BankAccountRes>> result = controller.list(
                enterpriseId, 0, 10, "accountNumber", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "asc", null);
    }

    @Test
    @DisplayName("Debe mapear correctamente la respuesta en todos los endpoints")
    void testMapperInvocation_InAllEndpoints() {
        // Arrange
        when(service.create(createRequest)).thenReturn(bankAccount);
        when(service.update(updateRequest)).thenReturn(bankAccount);
        when(service.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        when(service.changeState(bankAccountId, enterpriseId, false)).thenReturn(bankAccount);
        when(service.delete(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        when(mapper.toRes(bankAccount)).thenReturn(bankAccountRes);

        // Act
        controller.create(createRequest);
        controller.update(updateRequest);
        controller.getById(bankAccountId, enterpriseId);
        controller.changeState(bankAccountId, enterpriseId, false);
        controller.delete(bankAccountId, enterpriseId);

        // Assert
        verify(mapper, times(5)).toRes(bankAccount);
    }
}
