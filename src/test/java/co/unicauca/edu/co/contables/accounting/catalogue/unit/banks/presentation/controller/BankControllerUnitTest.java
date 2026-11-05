package co.unicauca.edu.co.contables.accounting.catalogue.unit.banks.presentation.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.mapper.BankDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.IBankService;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.response.BankRes;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.controller.BankController;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks.BankNotFoundException;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankControllerUnitTest {

    @Mock
    private IBankService service;

    @Mock
    private BankDomainMapper mapper;

    @InjectMocks
    private BankController controller;

    private Bank bank;
    private BankCreateReq createRequest;
    private BankUpdateReq updateRequest;
    private BankRes bankRes;
    private String enterpriseId;
    private Long bankId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        bankId = 1L;

        bank = Bank.builder()
                .id(bankId)
                .code("01")
                .name("BANCO TEST")
                .currencies(Set.of(Currency.COP, Currency.USD))
                .status(true)
                .idEnterprise(enterpriseId)
                .build();

        createRequest = BankCreateReq.builder()
                .idEnterprise(enterpriseId)
                .code("01")
                .name("Banco Test")
                .currencies(Set.of(Currency.COP, Currency.USD))
                .status(true)
                .build();

        updateRequest = BankUpdateReq.builder()
                .id(bankId)
                .idEnterprise(enterpriseId)
                .code("01")
                .name("Banco Test")
                .currencies(Set.of(Currency.COP, Currency.USD))
                .status(true)
                .build();

        bankRes = BankRes.builder()
                .id(bankId)
                .code("01")
                .name("BANCO TEST")
                .currencies(Set.of(Currency.COP, Currency.USD))
                .status(true)
                .idEnterprise(enterpriseId)
                .build();
    }

    @Test
    @DisplayName("Debe crear banco exitosamente")
    void testCreateSuccess() {
        // Arrange
        when(service.create(createRequest)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankId, result.getBody().getId());
        assertEquals("01", result.getBody().getCode());
        assertEquals("BANCO TEST", result.getBody().getName());
        verify(service).create(createRequest);
        verify(mapper).toRes(bank);
    }

    @Test
    @DisplayName("Debe actualizar banco exitosamente")
    void testUpdateSuccess() {
        // Arrange
        when(service.update(updateRequest)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankId, result.getBody().getId());
        verify(service).update(updateRequest);
        verify(mapper).toRes(bank);
    }

    @Test
    @DisplayName("Debe obtener banco por ID exitosamente")
    void testGetByIdSuccess() {
        // Arrange
        when(service.findById(bankId, enterpriseId)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.getById(bankId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankId, result.getBody().getId());
        assertEquals(enterpriseId, result.getBody().getIdEnterprise());
        verify(service).findById(bankId, enterpriseId);
        verify(mapper).toRes(bank);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener banco inexistente")
    void testGetById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(service.findById(bankId, enterpriseId))
                .thenThrow(new BankNotFoundException());

        // Act & Assert
        assertThrows(BankNotFoundException.class, () ->
                controller.getById(bankId, enterpriseId)
        );
        verify(service).findById(bankId, enterpriseId);
        verify(mapper, never()).toRes(any());
    }

    @Test
    @DisplayName("Debe listar bancos con filtros exitosamente")
    void testListWithFiltersSuccess() {
        // Arrange
        List<Bank> banks = List.of(bank);
        Page<Bank> banksPage = new PageImpl<>(banks);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "asc", null))
                .thenReturn(banksPage);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.list(
                enterpriseId, 0, 10, "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "asc", null);
    }

    @Test
    @DisplayName("Debe listar bancos con búsqueda")
    void testListWithSearchSuccess() {
        // Arrange
        String searchTerm = "TEST";
        List<Bank> banks = List.of(bank);
        Page<Bank> banksPage = new PageImpl<>(banks);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "asc", searchTerm))
                .thenReturn(banksPage);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.list(
                enterpriseId, 0, 10, "name", "asc", searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "asc", searchTerm);
    }

    @Test
    @DisplayName("Debe listar bancos con parámetros null")
    void testList_WithNullParams_Success() {
        // Arrange
        List<Bank> banks = List.of(bank);
        Page<Bank> banksPage = new PageImpl<>(banks);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, null, null, null, null, null))
                .thenReturn(banksPage);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.list(
                enterpriseId, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, null, null, null, null, null);
    }

    @Test
    @DisplayName("Debe listar bancos activos exitosamente")
    void testListActiveSuccess() {
        // Arrange
        List<Bank> banks = List.of(bank);
        Page<Bank> banksPage = new PageImpl<>(banks);

        when(service.findAllActiveByEnterprise(enterpriseId, 0, 10)).thenReturn(banksPage);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.listActive(enterpriseId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        assertTrue(result.getBody().getContent().get(0).getStatus());
        verify(service).findAllActiveByEnterprise(enterpriseId, 0, 10);
    }

    @Test
    @DisplayName("Debe listar bancos activos con parámetros null")
    void testListActive_WithNullParams_Success() {
        // Arrange
        List<Bank> banks = List.of(bank);
        Page<Bank> banksPage = new PageImpl<>(banks);

        when(service.findAllActiveByEnterprise(enterpriseId, null, null)).thenReturn(banksPage);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.listActive(enterpriseId, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllActiveByEnterprise(enterpriseId, null, null);
    }

    @Test
    @DisplayName("Debe cambiar estado de banco a inactivo exitosamente")
    void testChangeStateToInactiveSuccess() {
        // Arrange
        bank.setStatus(false);
        bankRes.setStatus(false);

        when(service.changeState(bankId, enterpriseId, false)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.changeState(bankId, enterpriseId, false);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().getStatus());
        verify(service).changeState(bankId, enterpriseId, false);
        verify(mapper).toRes(bank);
    }

    @Test
    @DisplayName("Debe cambiar estado de banco a activo exitosamente")
    void testChangeStateToActiveSuccess() {
        // Arrange
        when(service.changeState(bankId, enterpriseId, true)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.changeState(bankId, enterpriseId, true);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().getStatus());
        verify(service).changeState(bankId, enterpriseId, true);
        verify(mapper).toRes(bank);
    }

    @Test
    @DisplayName("Debe eliminar banco exitosamente")
    void testDeleteSuccess() {
        // Arrange
        when(service.delete(bankId, enterpriseId)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.delete(bankId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(bankId, result.getBody().getId());
        verify(service).delete(bankId, enterpriseId);
        verify(mapper).toRes(bank);
    }

    @Test
    @DisplayName("Debe propagar excepción al eliminar banco inexistente")
    void testDelete_WithNonExistentBank_ThrowsException() {
        // Arrange
        when(service.delete(bankId, enterpriseId))
                .thenThrow(new BankNotFoundException());

        // Act & Assert
        assertThrows(BankNotFoundException.class, () ->
                controller.delete(bankId, enterpriseId)
        );
        verify(service).delete(bankId, enterpriseId);
        verify(mapper, never()).toRes(any());
    }

    @Test
    @DisplayName("Debe crear banco con múltiples monedas")
    void testCreate_WithMultipleCurrencies_Success() {
        // Arrange
        createRequest.setCurrencies(Set.of(Currency.COP, Currency.USD, Currency.EUR));
        bank.setCurrencies(Set.of(Currency.COP, Currency.USD, Currency.EUR));
        bankRes.setCurrencies(Set.of(Currency.COP, Currency.USD, Currency.EUR));

        when(service.create(createRequest)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getBody().getCurrencies().size());
        verify(service).create(createRequest);
    }

    @Test
    @DisplayName("Debe actualizar código de banco")
    void testUpdate_ChangingCode_Success() {
        // Arrange
        updateRequest.setCode("02");
        bank.setCode("02");
        bankRes.setCode("02");

        when(service.update(updateRequest)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("02", result.getBody().getCode());
        verify(service).update(updateRequest);
    }

    @Test
    @DisplayName("Debe listar bancos con ordenamiento descendente")
    void testList_WithDescOrder_Success() {
        // Arrange
        List<Bank> banks = List.of(bank);
        Page<Bank> banksPage = new PageImpl<>(banks);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "desc", null))
                .thenReturn(banksPage);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.list(
                enterpriseId, 0, 10, "name", "desc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "desc", null);
    }

    @Test
    @DisplayName("Debe listar bancos con ordenamiento por código")
    void testList_WithCodeSorting_Success() {
        // Arrange
        List<Bank> banks = List.of(bank);
        Page<Bank> banksPage = new PageImpl<>(banks);

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "code", "asc", null))
                .thenReturn(banksPage);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.list(
                enterpriseId, 0, 10, "code", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "code", "asc", null);
    }

    @Test
    @DisplayName("Debe obtener banco de diferentes empresas")
    void testGetById_DifferentEnterprises_Success() {
        // Arrange
        String enterprise2 = "ENT-002";
        Bank bank2 = Bank.builder()
                .id(bankId)
                .idEnterprise(enterprise2)
                .build();
        BankRes res2 = BankRes.builder()
                .id(bankId)
                .idEnterprise(enterprise2)
                .build();

        when(service.findById(bankId, enterprise2)).thenReturn(bank2);
        when(mapper.toRes(bank2)).thenReturn(res2);

        // Act
        ResponseEntity<BankRes> result = controller.getById(bankId, enterprise2);

        // Assert
        assertNotNull(result);
        assertEquals(enterprise2, result.getBody().getIdEnterprise());
        verify(service).findById(bankId, enterprise2);
    }

    @Test
    @DisplayName("Debe crear múltiples bancos")
    void testCreate_MultipleBanks_Success() {
        // Arrange
        BankCreateReq request2 = BankCreateReq.builder()
                .idEnterprise(enterpriseId)
                .code("02")
                .name("Banco Dos")
                .currencies(Set.of(Currency.COP))
                .build();

        Bank bank2 = Bank.builder()
                .id(2L)
                .code("02")
                .name("BANCO DOS")
                .build();

        BankRes res2 = BankRes.builder()
                .id(2L)
                .code("02")
                .name("BANCO DOS")
                .build();

        when(service.create(createRequest)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);
        when(service.create(request2)).thenReturn(bank2);
        when(mapper.toRes(bank2)).thenReturn(res2);

        // Act
        ResponseEntity<BankRes> result1 = controller.create(createRequest);
        ResponseEntity<BankRes> result2 = controller.create(request2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1L, result1.getBody().getId());
        assertEquals(2L, result2.getBody().getId());
        verify(service, times(1)).create(createRequest);
        verify(service, times(1)).create(request2);
    }

    @Test
    @DisplayName("Debe listar bancos vacíos")
    void testList_EmptyResults_Success() {
        // Arrange
        Page<Bank> emptyPage = Page.empty();

        when(service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "asc", null))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<BankRes>> result = controller.list(
                enterpriseId, 0, 10, "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
        verify(service).findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "asc", null);
    }

    @Test
    @DisplayName("Debe mapear correctamente la respuesta en todos los endpoints")
    void testMapperInvocation_InAllEndpoints() {
        // Arrange
        when(service.create(createRequest)).thenReturn(bank);
        when(service.update(updateRequest)).thenReturn(bank);
        when(service.findById(bankId, enterpriseId)).thenReturn(bank);
        when(service.changeState(bankId, enterpriseId, false)).thenReturn(bank);
        when(service.delete(bankId, enterpriseId)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        controller.create(createRequest);
        controller.update(updateRequest);
        controller.getById(bankId, enterpriseId);
        controller.changeState(bankId, enterpriseId, false);
        controller.delete(bankId, enterpriseId);

        // Assert
        verify(mapper, times(5)).toRes(bank);
    }

    @Test
    @DisplayName("Debe actualizar nombre de banco")
    void testUpdate_ChangingName_Success() {
        // Arrange
        updateRequest.setName("Banco Actualizado");
        bank.setName("BANCO ACTUALIZADO");
        bankRes.setName("BANCO ACTUALIZADO");

        when(service.update(updateRequest)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("BANCO ACTUALIZADO", result.getBody().getName());
        verify(service).update(updateRequest);
    }

    @Test
    @DisplayName("Debe crear banco con una sola moneda")
    void testCreate_WithSingleCurrency_Success() {
        // Arrange
        createRequest.setCurrencies(Set.of(Currency.COP));
        bank.setCurrencies(Set.of(Currency.COP));
        bankRes.setCurrencies(Set.of(Currency.COP));

        when(service.create(createRequest)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBody().getCurrencies().size());
        assertTrue(result.getBody().getCurrencies().contains(Currency.COP));
        verify(service).create(createRequest);
    }

    @Test
    @DisplayName("Debe retornar OK en todos los endpoints exitosos")
    void testHttpStatus_AllEndpoints_ReturnOK() {
        // Arrange
        Page<Bank> banksPage = new PageImpl<>(List.of(bank));

        when(service.create(createRequest)).thenReturn(bank);
        when(service.update(updateRequest)).thenReturn(bank);
        when(service.findById(bankId, enterpriseId)).thenReturn(bank);
        when(service.findAllByEnterpriseWithFilters(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(banksPage);
        when(service.findAllActiveByEnterprise(anyString(), any(), any())).thenReturn(banksPage);
        when(service.changeState(bankId, enterpriseId, false)).thenReturn(bank);
        when(service.delete(bankId, enterpriseId)).thenReturn(bank);
        when(mapper.toRes(bank)).thenReturn(bankRes);

        // Act
        ResponseEntity<BankRes> createResult = controller.create(createRequest);
        ResponseEntity<BankRes> updateResult = controller.update(updateRequest);
        ResponseEntity<BankRes> getByIdResult = controller.getById(bankId, enterpriseId);
        ResponseEntity<Page<BankRes>> listResult = controller.list(enterpriseId, 0, 10, "name", "asc", null);
        ResponseEntity<Page<BankRes>> listActiveResult = controller.listActive(enterpriseId, 0, 10);
        ResponseEntity<BankRes> changeStateResult = controller.changeState(bankId, enterpriseId, false);
        ResponseEntity<BankRes> deleteResult = controller.delete(bankId, enterpriseId);

        // Assert
        assertEquals(HttpStatus.OK, createResult.getStatusCode());
        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        assertEquals(HttpStatus.OK, getByIdResult.getStatusCode());
        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertEquals(HttpStatus.OK, listActiveResult.getStatusCode());
        assertEquals(HttpStatus.OK, changeStateResult.getStatusCode());
        assertEquals(HttpStatus.OK, deleteResult.getStatusCode());
    }
}
