package co.unicauca.edu.co.contables.accounting.catalogue.unit.banks.domain.services;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.entity.BankAccountEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository.BankAccountRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.mapper.BankDataMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.repository.BankRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.mapper.BankDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.BankServiceImpl;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks.*;
import co.unicauca.edu.co.contables.commons.utils.PaginationHelper;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankServiceImplUnitTest {

    @Mock
    private BankRepository repository;

    @Mock
    private BankDataMapper dataMapper;

    @Mock
    private BankDomainMapper domainMapper;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private BankServiceImpl service;

    private BankCreateReq createRequest;
    private BankUpdateReq updateRequest;
    private Bank bank;
    private BankEntity bankEntity;
    private String enterpriseId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";

        createRequest = BankCreateReq.builder()
                .idEnterprise(enterpriseId)
                .code("01")
                .name("banco test")
                .currencies(Set.of(Currency.COP, Currency.USD))
                .status(true)
                .build();

        updateRequest = BankUpdateReq.builder()
                .id(1L)
                .idEnterprise(enterpriseId)
                .code("01")
                .name("banco test")
                .currencies(Set.of(Currency.COP, Currency.USD))
                .status(true)
                .build();

        bank = Bank.builder()
                .id(1L)
                .code("01")
                .name("BANCO TEST")
                .currencies(Set.of(Currency.COP, Currency.USD))
                .status(true)
                .idEnterprise(enterpriseId)
                .build();

        bankEntity = new BankEntity();
        bankEntity.setId(1L);
        bankEntity.setCode("01");
        bankEntity.setName("BANCO TEST");
        bankEntity.setCurrencies(Set.of(Currency.COP, Currency.USD));
        bankEntity.setStatus(true);
        bankEntity.setIdEnterprise(enterpriseId);
    }

    @Test
    @DisplayName("Debe crear banco exitosamente")
    void testCreateSuccess() {
        // Arrange
        when(repository.existsByCodeAndIdEnterprise("01", enterpriseId)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise("BANCO TEST", enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bank);
        when(dataMapper.toEntity(bank)).thenReturn(bankEntity);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("BANCO TEST", result.getName());
        verify(repository).save(any(BankEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código es null")
    void testCreate_WithNullCode_ThrowsException() {
        // Arrange
        createRequest.setCode(null);

        // Act & Assert
        assertThrows(InvalidBankCodeException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código está vacío")
    void testCreate_WithEmptyCode_ThrowsException() {
        // Arrange
        createRequest.setCode("");

        // Act & Assert
        assertThrows(InvalidBankCodeException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código tiene más de 2 dígitos")
    void testCreate_WithCodeMoreThan2Digits_ThrowsException() {
        // Arrange
        createRequest.setCode("001");

        // Act & Assert
        assertThrows(InvalidBankCodeException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código tiene menos de 2 dígitos")
    void testCreate_WithCodeLessThan2Digits_ThrowsException() {
        // Arrange
        createRequest.setCode("1");

        // Act & Assert
        assertThrows(InvalidBankCodeException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código contiene letras")
    void testCreate_WithCodeContainingLetters_ThrowsException() {
        // Arrange
        createRequest.setCode("A1");

        // Act & Assert
        assertThrows(InvalidBankCodeException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código es 00")
    void testCreate_WithCode00_ThrowsException() {
        // Arrange
        createRequest.setCode("00");

        // Act & Assert
        assertThrows(InvalidBankCodeException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código ya existe")
    void testCreate_WithDuplicateCode_ThrowsException() {
        // Arrange
        when(repository.existsByCodeAndIdEnterprise("01", enterpriseId)).thenReturn(true);

        // Act & Assert
        assertThrows(BankAlreadyExistsException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando nombre ya existe")
    void testCreate_WithDuplicateName_ThrowsException() {
        // Arrange
        when(repository.existsByCodeAndIdEnterprise("01", enterpriseId)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise("BANCO TEST", enterpriseId)).thenReturn(true);

        // Act & Assert
        assertThrows(BankAlreadyExistsException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe estandarizar nombre a mayúsculas al crear")
    void testCreate_StandardizesNameToUppercase() {
        // Arrange
        createRequest.setName("banco test");
        when(repository.existsByCodeAndIdEnterprise("01", enterpriseId)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise("BANCO TEST", enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bank);
        when(dataMapper.toEntity(any(Bank.class))).thenReturn(bankEntity);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Bank> bankCaptor = ArgumentCaptor.forClass(Bank.class);
        verify(dataMapper).toEntity(bankCaptor.capture());
        assertEquals("BANCO TEST", bankCaptor.getValue().getName());
    }

    @Test
    @DisplayName("Debe crear banco con código 99")
    void testCreate_WithCode99_Success() {
        // Arrange
        createRequest.setCode("99");
        when(repository.existsByCodeAndIdEnterprise("99", enterpriseId)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise("BANCO TEST", enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bank);
        when(dataMapper.toEntity(bank)).thenReturn(bankEntity);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankEntity.class));
    }

    @Test
    @DisplayName("Debe actualizar banco exitosamente")
    void testUpdateSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar banco que no existe")
    void testUpdate_WithNonExistentBank_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankNotFoundException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar banco con cuentas con movimientos")
    void testUpdate_WithBankInUse_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BankInUseException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar banco cambiando código")
    void testUpdate_WithDifferentCode_Success() {
        // Arrange
        updateRequest.setCode("02");
        bankEntity.setCode("01");
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);
        when(repository.existsByCodeAndIdEnterprise("02", enterpriseId)).thenReturn(false);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<BankEntity> entityCaptor = ArgumentCaptor.forClass(BankEntity.class);
        verify(repository).save(entityCaptor.capture());
        assertEquals("02", entityCaptor.getValue().getCode());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con código duplicado")
    void testUpdate_WithDuplicateCode_ThrowsException() {
        // Arrange
        updateRequest.setCode("02");
        bankEntity.setCode("01");
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);
        when(repository.existsByCodeAndIdEnterprise("02", enterpriseId)).thenReturn(true);

        // Act & Assert
        assertThrows(BankAlreadyExistsException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar banco cambiando nombre")
    void testUpdate_WithDifferentName_Success() {
        // Arrange
        updateRequest.setName("banco actualizado");
        bankEntity.setName("BANCO TEST");
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);
        when(repository.existsByNameAndIdEnterpriseAndIdNot("BANCO ACTUALIZADO", enterpriseId, 1L))
                .thenReturn(false);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<BankEntity> entityCaptor = ArgumentCaptor.forClass(BankEntity.class);
        verify(repository).save(entityCaptor.capture());
        assertEquals("BANCO ACTUALIZADO", entityCaptor.getValue().getName());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con nombre duplicado")
    void testUpdate_WithDuplicateName_ThrowsException() {
        // Arrange
        updateRequest.setName("banco duplicado");
        bankEntity.setName("BANCO TEST");
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);
        when(repository.existsByNameAndIdEnterpriseAndIdNot("BANCO DUPLICADO", enterpriseId, 1L))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BankAlreadyExistsException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe buscar banco por ID exitosamente")
    void testFindByIdSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.findById(1L, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).findByIdAndIdEnterprise(1L, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando banco no existe")
    void testFindById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankNotFoundException.class, () -> service.findById(1L, enterpriseId));
    }

    @Test
    @DisplayName("Debe obtener bancos paginados con búsqueda")
    void testFindAllByEnterpriseWithFilters_WithSearch_Success() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterpriseAndSearch(enterpriseId, "TEST")).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findByIdEnterpriseAndSearch(eq(enterpriseId), eq("TEST"), any(Pageable.class)))
                .thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "name", "asc", "TEST");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByIdEnterpriseAndSearch(eq(enterpriseId), eq("TEST"), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener bancos paginados sin búsqueda")
    void testFindAllByEnterpriseWithFilters_WithoutSearch_Success() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener bancos con ordenamiento por código ascendente")
    void testFindAllByEnterpriseWithFilters_WithCodeAscOrder_Success() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "code", "asc", null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertEquals(Sort.Direction.ASC, pageableCaptor.getValue().getSort().getOrderFor("code").getDirection());
    }

    @Test
    @DisplayName("Debe obtener bancos con ordenamiento descendente")
    void testFindAllByEnterpriseWithFilters_WithDescOrder_Success() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "name", "desc", null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("name").getDirection());
    }

    @Test
    @DisplayName("Debe lanzar excepción con campo de ordenamiento inválido")
    void testFindAllByEnterpriseWithFilters_WithInvalidSortField_ThrowsException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "invalidField", "asc", null)
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción con orden de ordenamiento inválido")
    void testFindAllByEnterpriseWithFilters_WithInvalidSortOrder_ThrowsException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "name", "invalid", null)
        );
    }

    @Test
    @DisplayName("Debe usar campo de ordenamiento por defecto cuando es null")
    void testFindAllByEnterpriseWithFilters_WithNullSortField_UsesDefault() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, null, "asc", null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertNotNull(pageableCaptor.getValue().getSort().getOrderFor("name"));
    }

    @Test
    @DisplayName("Debe obtener bancos activos paginados")
    void testFindAllActiveByEnterpriseSuccess() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterpriseAndStatus(enterpriseId, true)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterpriseAndStatus(eq(enterpriseId), eq(true), any(Pageable.class)))
                .thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllActiveByEnterprise(enterpriseId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByIdEnterpriseAndStatus(eq(enterpriseId), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe cambiar estado de banco exitosamente")
    void testChangeStateSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.changeState(1L, enterpriseId, false);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<BankEntity> entityCaptor = ArgumentCaptor.forClass(BankEntity.class);
        verify(repository).save(entityCaptor.capture());
        assertEquals(false, entityCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado de banco no existente")
    void testChangeState_WithNonExistentBank_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankNotFoundException.class, () -> service.changeState(1L, enterpriseId, false));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar banco exitosamente")
    void testDeleteSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.findAllByIdEnterpriseAndBankId(eq(enterpriseId), eq(1L), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.delete(1L, enterpriseId);

        // Assert
        assertNotNull(result);
        verify(repository).delete(bankEntity);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar banco que no existe")
    void testDelete_WithNonExistentBank_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankNotFoundException.class, () -> service.delete(1L, enterpriseId));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar banco con cuentas asociadas")
    void testDelete_WithAssociatedAccounts_ThrowsException() {
        // Arrange
        BankAccountEntity mockAccount = new BankAccountEntity();
        mockAccount.setId(1L);
        Page<BankAccountEntity> mockPage = new PageImpl<>(Collections.singletonList(mockAccount));
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.findAllByIdEnterpriseAndBankId(eq(enterpriseId), eq(1L), any(PageRequest.class)))
                .thenReturn(mockPage);

        // Act & Assert
        assertThrows(BankHasAssociatedAccountsException.class, () -> service.delete(1L, enterpriseId));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe estandarizar nombre con espacios al crear")
    void testCreate_StandardizesNameWithSpaces() {
        // Arrange
        createRequest.setName("  banco   test  ");
        when(repository.existsByCodeAndIdEnterprise("01", enterpriseId)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise("BANCO   TEST", enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bank);
        when(dataMapper.toEntity(any(Bank.class))).thenReturn(bankEntity);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Bank> bankCaptor = ArgumentCaptor.forClass(Bank.class);
        verify(dataMapper).toEntity(bankCaptor.capture());
        assertTrue(bankCaptor.getValue().getName().startsWith("BANCO"));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con código inválido")
    void testUpdate_WithInvalidCode_ThrowsException() {
        // Arrange
        updateRequest.setCode("ABC");
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);

        // Act & Assert
        assertThrows(InvalidBankCodeException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar banco sin cambiar código cuando es el mismo")
    void testUpdate_WithSameCode_DoesNotCheckDuplicate() {
        // Arrange
        bankEntity.setCode("01");
        updateRequest.setCode("01");
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository, never()).existsByCodeAndIdEnterprise(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe actualizar banco sin cambiar nombre cuando es el mismo")
    void testUpdate_WithSameName_DoesNotCheckDuplicate() {
        // Arrange
        bankEntity.setName("BANCO TEST");
        updateRequest.setName("banco test");
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankEntity));
        when(bankAccountRepository.existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(1L, enterpriseId))
                .thenReturn(false);
        when(repository.save(any(BankEntity.class))).thenReturn(bankEntity);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Bank result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository, never()).existsByNameAndIdEnterpriseAndIdNot(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Debe obtener bancos con ordenamiento por nombre cuando sortField es vacío")
    void testFindAllByEnterpriseWithFilters_WithEmptySortField_UsesDefaultName() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "", "asc", null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertNotNull(pageableCaptor.getValue().getSort().getOrderFor("name"));
    }

    @Test
    @DisplayName("Debe usar ordenamiento ascendente por defecto cuando sortOrder es null")
    void testFindAllByEnterpriseWithFilters_WithNullSortOrder_UsesAscending() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "name", null, null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertEquals(Sort.Direction.ASC, pageableCaptor.getValue().getSort().getOrderFor("name").getDirection());
    }

    @Test
    @DisplayName("Debe usar ordenamiento ascendente cuando sortOrder está vacío")
    void testFindAllByEnterpriseWithFilters_WithEmptySortOrder_UsesAscending() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "name", "", null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertEquals(Sort.Direction.ASC, pageableCaptor.getValue().getSort().getOrderFor("name").getDirection());
    }

    @Test
    @DisplayName("Debe trimear búsqueda antes de consultar")
    void testFindAllByEnterpriseWithFilters_TrimsSearchTerm() {
        // Arrange
        Page<BankEntity> page = new PageImpl<>(Collections.singletonList(bankEntity));

        when(repository.countByIdEnterpriseAndSearch(enterpriseId, "TEST")).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findByIdEnterpriseAndSearch(eq(enterpriseId), eq("TEST"), any(Pageable.class)))
                .thenReturn(page);
        when(dataMapper.toDomain(bankEntity)).thenReturn(bank);

        // Act
        Page<Bank> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "name", "asc", "  TEST  ");

        // Assert
        assertNotNull(result);
        verify(repository).findByIdEnterpriseAndSearch(eq(enterpriseId), eq("TEST"), any(Pageable.class));
    }
}
