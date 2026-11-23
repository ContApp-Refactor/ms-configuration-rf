package co.unicauca.edu.co.contables.accounting.catalogue.unit.paymentMethods.domain.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.InvalidAccountingAccountException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodInUseException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import co.unicauca.edu.co.contables.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.entity.PaymentMethodEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.mapper.PaymentMethodDataMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.repository.PaymentMethodRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.mapper.PaymentMethodDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.PaymentMethodServiceImpl;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentMethodServiceImplUnitTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodDataMapper dataMapper;

    @Mock
    private PaymentMethodDomainMapper domainMapper;

    @Mock
    private AccountCatalogueValidationService accountCatalogueValidationService;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private PaymentMethodServiceImpl service;

    private PaymentMethodCreateReq createRequest;
    private PaymentMethodUpdateReq updateRequest;
    private PaymentMethod paymentMethod;
    private PaymentMethodEntity paymentMethodEntity;
    private AccountCatalogue accountCatalogue;
    private AccountCatalogueEntity accountCatalogueEntity;
    private String enterpriseId;
    private static final String ACCOUNT_CODE = "11050101";

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";

        createRequest = PaymentMethodCreateReq.builder()
                .idEnterprise(enterpriseId)
                .name("Efectivo")
                .accountingAccountId(1L)
                .status(true)
                .build();

        updateRequest = PaymentMethodUpdateReq.builder()
                .id(1L)
                .idEnterprise(enterpriseId)
                .name("Efectivo Actualizado")
                .accountingAccountId(1L)
                .build();

        accountCatalogue = AccountCatalogue.builder()
                .id(1L)
                .code(ACCOUNT_CODE)
                .description("Caja General")
                .build();

        accountCatalogueEntity = new AccountCatalogueEntity();
        accountCatalogueEntity.setId(1L);
        accountCatalogueEntity.setCode(ACCOUNT_CODE);
        accountCatalogueEntity.setDescription("Caja General");

        paymentMethod = PaymentMethod.builder()
                .id(1L)
                .name("EFECTIVO")
                .accountingAccount(ACCOUNT_CODE)
                .accountingAccountEntity(accountCatalogue)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();

        paymentMethodEntity = PaymentMethodEntity.builder()
                .id(1L)
                .name("EFECTIVO")
                .accountingAccount(accountCatalogueEntity)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe crear método de pago con datos válidos")
    void testCreate_WithValidData_CreatesSuccessfully() {
        // Arrange
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountCatalogue);
        when(accountCatalogueValidationService.validateAccountExists(ACCOUNT_CODE, enterpriseId))
                .thenReturn(accountCatalogue);
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(domainMapper.toDomain(createRequest)).thenReturn(paymentMethod);
        when(dataMapper.toEntity(paymentMethod)).thenReturn(paymentMethodEntity);
        when(repository.save(any(PaymentMethodEntity.class))).thenReturn(paymentMethodEntity);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        PaymentMethod result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("EFECTIVO", result.getName());
        verify(accountCatalogueValidationService).validateAccountExistsByIdAndEnterprise(1L, enterpriseId);
        verify(accountCatalogueValidationService).validateAccountExists(ACCOUNT_CODE, enterpriseId);
        verify(repository).save(any(PaymentMethodEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta contable no existe por ID")
    void testCreate_WithNonExistentAccountId_ThrowsException() {
        // Arrange
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenThrow(new InvalidAccountingAccountException("Cuenta no encontrada"));

        // Act & Assert
        assertThrows(InvalidAccountingAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta no es auxiliar")
    void testCreate_WithNonAuxiliaryAccount_ThrowsException() {
        // Arrange
        AccountCatalogue nonAuxAccount = AccountCatalogue.builder()
                .id(1L)
                .code("1105") // Código de 4 dígitos, no auxiliar
                .description("Cuenta Mayor")
                .build();
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(nonAuxAccount);

        // Act & Assert
        assertThrows(InvalidAccountingAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre ya existe")
    void testCreate_WithDuplicateName_ThrowsException() {
        // Arrange
        PaymentMethodEntity existingMethod = PaymentMethodEntity.builder()
                .id(2L)
                .name("efectivo")
                .idEnterprise(enterpriseId)
                .build();
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountCatalogue);
        when(accountCatalogueValidationService.validateAccountExists(ACCOUNT_CODE, enterpriseId))
                .thenReturn(accountCatalogue);
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existingMethod)));

        // Act & Assert
        assertThrows(PaymentMethodsAlreadyExistsException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar método de pago con datos válidos")
    void testUpdate_WithValidData_UpdatesSuccessfully() {
        // Arrange
        PaymentMethodEntity entityWithoutUsage = PaymentMethodEntity.builder()
                .id(1L)
                .name("EFECTIVO")
                .accountingAccount(accountCatalogueEntity)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
        PaymentMethod methodWithoutUsage = PaymentMethod.builder()
                .id(1L)
                .name("EFECTIVO")
                .accountingAccount(ACCOUNT_CODE)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();

        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(entityWithoutUsage));
        when(dataMapper.toDomain(entityWithoutUsage)).thenReturn(methodWithoutUsage);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountCatalogue);
        when(accountCatalogueValidationService.validateAccountExists(ACCOUNT_CODE, enterpriseId))
                .thenReturn(accountCatalogue);
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(repository.save(any(PaymentMethodEntity.class))).thenReturn(entityWithoutUsage);
        when(dataMapper.toDomain(any(PaymentMethodEntity.class))).thenReturn(methodWithoutUsage);

        // Act
        PaymentMethod result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).findByIdAndIdEnterprise(1L, enterpriseId);
        verify(repository).save(any(PaymentMethodEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar método de pago inexistente")
    void testUpdate_WithNonExistentPaymentMethod_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar método de pago en uso")
    void testUpdate_WithPaymentMethodInUse_ThrowsException() {
        // Arrange
        PaymentMethodEntity entityInUse = PaymentMethodEntity.builder()
                .id(1L)
                .name("EFECTIVO")
                .accountingAccount(accountCatalogueEntity)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(5)
                .build();
        PaymentMethod methodInUse = PaymentMethod.builder()
                .id(1L)
                .name("EFECTIVO")
                .usageCount(5)
                .build();

        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(entityInUse));
        when(dataMapper.toDomain(entityInUse)).thenReturn(methodInUse);

        // Act & Assert
        assertThrows(PaymentMethodInUseException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con nombre duplicado")
    void testUpdate_WithDuplicateName_ThrowsException() {
        // Arrange
        PaymentMethodEntity current = PaymentMethodEntity.builder()
                .id(1L)
                .name("EFECTIVO")
                .accountingAccount(accountCatalogueEntity)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
        PaymentMethodEntity other = PaymentMethodEntity.builder()
                .id(2L)
                .name("Efectivo Actualizado")
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
        PaymentMethod method = PaymentMethod.builder()
                .id(1L)
                .usageCount(0)
                .build();

        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(current));
        when(dataMapper.toDomain(current)).thenReturn(method);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountCatalogue);
        when(accountCatalogueValidationService.validateAccountExists(ACCOUNT_CODE, enterpriseId))
                .thenReturn(accountCatalogue);
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(current, other)));

        // Act & Assert
        assertThrows(PaymentMethodsAlreadyExistsException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe encontrar método de pago por ID y empresa")
    void testFindById_WithValidData_ReturnsPaymentMethod() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(paymentMethodEntity));
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        PaymentMethod result = service.findById(1L, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EFECTIVO", result.getName());
        verify(repository).findByIdAndIdEnterprise(1L, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra método de pago por ID")
    void testFindById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () -> service.findById(1L, enterpriseId));
    }

    @Test
    @DisplayName("Debe listar todos los métodos de pago por empresa sin búsqueda")
    void testFindAllByEnterprise_WithoutSearch_ReturnsPagedResults() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<PaymentMethodEntity> entityPage = new PageImpl<>(
                List.of(paymentMethodEntity),
                pageable,
                1L
        );

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(0), Optional.of(10), 1L))
                .thenReturn(pageable);
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        Page<PaymentMethod> result = service.findAllByEnterprise(
                enterpriseId,
                Optional.of(0),
                Optional.of(10),
                "name",
                "asc",
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(repository).countByIdEnterprise(enterpriseId);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe listar métodos de pago con búsqueda")
    void testFindAllByEnterprise_WithSearch_ReturnsFilteredResults() {
        // Arrange
        String search = "Efectivo";
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<PaymentMethodEntity> entityPage = new PageImpl<>(
                List.of(paymentMethodEntity),
                pageable,
                1L
        );

        when(repository.countByIdEnterpriseAndSearch(enterpriseId, search)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(0), Optional.of(10), 1L))
                .thenReturn(pageable);
        when(repository.findByIdEnterpriseAndSearch(eq(enterpriseId), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        Page<PaymentMethod> result = service.findAllByEnterprise(
                enterpriseId,
                Optional.of(0),
                Optional.of(10),
                "name",
                "asc",
                search
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).countByIdEnterpriseAndSearch(enterpriseId, search);
        verify(repository).findByIdEnterpriseAndSearch(eq(enterpriseId), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe listar solo métodos de pago activos")
    void testFindAllActiveByEnterprise_ReturnsOnlyActivePaymentMethods() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<PaymentMethodEntity> entityPage = new PageImpl<>(
                List.of(paymentMethodEntity),
                pageable,
                1L
        );

        when(repository.countByIdEnterpriseAndStatus(enterpriseId, true)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(0), Optional.of(10), 1L))
                .thenReturn(pageable);
        when(repository.findAllByIdEnterpriseAndStatus(eq(enterpriseId), eq(true), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        Page<PaymentMethod> result = service.findAllActiveByEnterprise(
                enterpriseId,
                Optional.of(0),
                Optional.of(10)
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getStatus());
        verify(repository).countByIdEnterpriseAndStatus(enterpriseId, true);
        verify(repository).findAllByIdEnterpriseAndStatus(eq(enterpriseId), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe cambiar estado del método de pago")
    void testChangeState_WithValidData_ChangesStateSuccessfully() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(paymentMethodEntity));
        when(repository.save(any(PaymentMethodEntity.class))).thenReturn(paymentMethodEntity);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        PaymentMethod result = service.changeState(1L, enterpriseId, false);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<PaymentMethodEntity> captor = ArgumentCaptor.forClass(PaymentMethodEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(false, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado de método inexistente")
    void testChangeState_WithNonExistentPaymentMethod_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class,
                () -> service.changeState(1L, enterpriseId, false));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar método de pago sin uso")
    void testDelete_WithUnusedPaymentMethod_DeletesSuccessfully() {
        // Arrange
        PaymentMethodEntity entityWithoutUsage = PaymentMethodEntity.builder()
                .id(1L)
                .name("EFECTIVO")
                .usageCount(0)
                .build();
        PaymentMethod methodWithoutUsage = PaymentMethod.builder()
                .id(1L)
                .name("EFECTIVO")
                .usageCount(0)
                .build();

        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(entityWithoutUsage));
        when(dataMapper.toDomain(entityWithoutUsage)).thenReturn(methodWithoutUsage);
        doNothing().when(repository).delete(entityWithoutUsage);

        // Act
        PaymentMethod result = service.delete(1L, enterpriseId);

        // Assert
        assertNotNull(result);
        verify(repository).delete(entityWithoutUsage);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar método de pago inexistente")
    void testDelete_WithNonExistentPaymentMethod_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () -> service.delete(1L, enterpriseId));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar método de pago en uso")
    void testDelete_WithPaymentMethodInUse_ThrowsException() {
        // Arrange
        PaymentMethodEntity entityInUse = PaymentMethodEntity.builder()
                .id(1L)
                .name("EFECTIVO")
                .usageCount(3)
                .build();
        PaymentMethod methodInUse = PaymentMethod.builder()
                .id(1L)
                .name("EFECTIVO")
                .usageCount(3)
                .build();

        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(entityInUse));
        when(dataMapper.toDomain(entityInUse)).thenReturn(methodInUse);

        // Act & Assert
        assertThrows(PaymentMethodInUseException.class, () -> service.delete(1L, enterpriseId));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe actualizar contador de uso")
    void testUpdateUsageCount_WithValidData_UpdatesSuccessfully() {
        // Arrange
        Integer newUsageCount = 5;
        when(repository.findById(1L)).thenReturn(Optional.of(paymentMethodEntity));
        when(repository.save(any(PaymentMethodEntity.class))).thenReturn(paymentMethodEntity);

        // Act
        service.updateUsageCount(1L, newUsageCount);

        // Assert
        ArgumentCaptor<PaymentMethodEntity> captor = ArgumentCaptor.forClass(PaymentMethodEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(newUsageCount, captor.getValue().getUsageCount());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar contador de método inexistente")
    void testUpdateUsageCount_WithNonExistentPaymentMethod_ThrowsException() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentMethodsNotFoundException.class, () -> service.updateUsageCount(1L, 5));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar cuenta contable con código null")
    void testCreate_WithNullAccountCode_ThrowsException() {
        // Arrange
        AccountCatalogue accountWithNullCode = AccountCatalogue.builder()
                .id(1L)
                .code(null)
                .description("Cuenta sin código")
                .build();
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountWithNullCode);

        // Act & Assert
        assertThrows(InvalidAccountingAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar cuenta contable con código vacío")
    void testCreate_WithEmptyAccountCode_ThrowsException() {
        // Arrange
        AccountCatalogue accountWithEmptyCode = AccountCatalogue.builder()
                .id(1L)
                .code("   ")
                .description("Cuenta con código vacío")
                .build();
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountWithEmptyCode);

        // Act & Assert
        assertThrows(InvalidAccountingAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar cuenta contable con formato inválido")
    void testCreate_WithInvalidAccountFormat_ThrowsException() {
        // Arrange
        AccountCatalogue accountWithInvalidFormat = AccountCatalogue.builder()
                .id(1L)
                .code("ABC12345")
                .description("Cuenta con formato inválido")
                .build();
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountWithInvalidFormat);

        // Act & Assert
        assertThrows(InvalidAccountingAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear método de pago con nombre en minúsculas")
    void testCreate_WithLowercaseName_CreatesSuccessfully() {
        // Arrange
        PaymentMethodCreateReq requestWithLowercase = PaymentMethodCreateReq.builder()
                .idEnterprise(enterpriseId)
                .name("efectivo")
                .accountingAccountId(1L)
                .status(true)
                .build();

        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountCatalogue);
        when(accountCatalogueValidationService.validateAccountExists(ACCOUNT_CODE, enterpriseId))
                .thenReturn(accountCatalogue);
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(domainMapper.toDomain(requestWithLowercase)).thenReturn(paymentMethod);
        when(dataMapper.toEntity(paymentMethod)).thenReturn(paymentMethodEntity);
        when(repository.save(any(PaymentMethodEntity.class))).thenReturn(paymentMethodEntity);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        PaymentMethod result = service.create(requestWithLowercase);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(PaymentMethodEntity.class));
    }

    @Test
    @DisplayName("Debe listar métodos de pago con ordenamiento descendente")
    void testFindAllByEnterprise_WithDescendingSort_ReturnsSortedResults() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<PaymentMethodEntity> entityPage = new PageImpl<>(
                List.of(paymentMethodEntity),
                pageable,
                1L
        );

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(0), Optional.of(10), 1L))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        Page<PaymentMethod> result = service.findAllByEnterprise(
                enterpriseId,
                Optional.of(0),
                Optional.of(10),
                "name",
                "desc",
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe permitir actualizar con el mismo nombre")
    void testUpdate_WithSameName_UpdatesSuccessfully() {
        // Arrange
        PaymentMethodEntity entityWithoutUsage = PaymentMethodEntity.builder()
                .id(1L)
                .name("Efectivo Actualizado")
                .accountingAccount(accountCatalogueEntity)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
        PaymentMethod methodWithoutUsage = PaymentMethod.builder()
                .id(1L)
                .name("Efectivo Actualizado")
                .usageCount(0)
                .build();

        when(repository.findByIdAndIdEnterprise(1L, enterpriseId))
                .thenReturn(Optional.of(entityWithoutUsage));
        when(dataMapper.toDomain(entityWithoutUsage)).thenReturn(methodWithoutUsage);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountCatalogue);
        when(accountCatalogueValidationService.validateAccountExists(ACCOUNT_CODE, enterpriseId))
                .thenReturn(accountCatalogue);
        when(repository.save(any(PaymentMethodEntity.class))).thenReturn(entityWithoutUsage);
        when(dataMapper.toDomain(any(PaymentMethodEntity.class))).thenReturn(methodWithoutUsage);

        // Act
        PaymentMethod result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(PaymentMethodEntity.class));
    }

    @Test
    @DisplayName("Debe listar métodos de pago con búsqueda trimmed")
    void testFindAllByEnterprise_WithTrimmedSearch_ReturnsFilteredResults() {
        // Arrange
        String searchWithSpaces = "  Efectivo  ";
        String trimmedSearch = "Efectivo";
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<PaymentMethodEntity> entityPage = new PageImpl<>(
                List.of(paymentMethodEntity),
                pageable,
                1L
        );

        when(repository.countByIdEnterpriseAndSearch(enterpriseId, trimmedSearch)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(0), Optional.of(10), 1L))
                .thenReturn(pageable);
        when(repository.findByIdEnterpriseAndSearch(eq(enterpriseId), eq(trimmedSearch), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(paymentMethodEntity)).thenReturn(paymentMethod);

        // Act
        Page<PaymentMethod> result = service.findAllByEnterprise(
                enterpriseId,
                Optional.of(0),
                Optional.of(10),
                "name",
                "asc",
                searchWithSpaces
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByIdEnterpriseAndSearch(eq(enterpriseId), eq(trimmedSearch), any(Pageable.class));
    }
}
