package co.unicauca.edu.co.contables.accounting.catalogue.unit.bankAccounts.domain.services;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.entity.BankAccountEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.mapper.BankAccountDataMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository.BankAccountRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.mapper.BankAccountDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.BankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.IBankService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts.*;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankAccountServiceUnitTest {

    @Mock
    private BankAccountRepository repository;

    @Mock
    private BankAccountDataMapper dataMapper;

    @Mock
    private BankAccountDomainMapper domainMapper;

    @Mock
    private IBankService bankService;

    @Mock
    private AccountCatalogueValidationService accountCatalogueValidationService;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private BankAccountService service;

    private BankAccountCreateReq createRequest;
    private BankAccountUpdateReq updateRequest;
    private BankAccount bankAccount;
    private BankAccountEntity bankAccountEntity;
    private Bank bank;
    private AccountCatalogue accountingAccount;
    private String enterpriseId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";

        bank = Bank.builder()
                .id(1L)
                .code("001")
                .name("Banco Test")
                .status(true)
                .idEnterprise(enterpriseId)
                .build();

        accountingAccount = AccountCatalogue.builder()
                .id(1L)
                .code("11050501")
                .description("Bancos")
                .status(true)
                .idEnterprise(enterpriseId)
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
                .id(1L)
                .idEnterprise(enterpriseId)
                .accountNumber(12345678L)
                .bankId(1L)
                .accountType(AccountType.AHORROS)
                .accountingAccountId(1L)
                .status(true)
                .build();

        bankAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(12345678L)
                .bank(bank)
                .accountType(AccountType.AHORROS)
                .accountingAccountId(1L)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();

        bankAccountEntity = new BankAccountEntity();
        bankAccountEntity.setId(1L);
        bankAccountEntity.setAccountNumber(12345678L);
        bankAccountEntity.setAccountType(AccountType.AHORROS);
        bankAccountEntity.setStatus(true);
        bankAccountEntity.setIdEnterprise(enterpriseId);
        bankAccountEntity.setUsageCount(0);

        BankEntity bankEntity = new BankEntity();
        bankEntity.setId(1L);
        bankAccountEntity.setBank(bankEntity);
    }

    @Test
    @DisplayName("Debe crear cuenta bancaria exitosamente")
    void testCreateSuccess() {
        // Arrange
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.existsByAccountNumberAndIdEnterprise(12345678L, enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bankAccount);
        when(dataMapper.toEntity(any(BankAccount.class))).thenReturn(bankAccountEntity);
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        BankAccount result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(12345678L, result.getAccountNumber());
        verify(repository).save(any(BankAccountEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando número de cuenta es null")
    void testCreate_WithNullAccountNumber_ThrowsException() {
        // Arrange
        createRequest.setAccountNumber(null);

        // Act & Assert
        assertThrows(InvalidAccountNumberException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando número de cuenta tiene menos de 8 dígitos")
    void testCreate_WithShortAccountNumber_ThrowsException() {
        // Arrange
        createRequest.setAccountNumber(1234567L); // 7 dígitos

        // Act & Assert
        assertThrows(InvalidAccountNumberException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando número de cuenta tiene más de 16 dígitos")
    void testCreate_WithLongAccountNumber_ThrowsException() {
        // Arrange
        createRequest.setAccountNumber(12345678901234567L); // 17 dígitos

        // Act & Assert
        assertThrows(InvalidAccountNumberException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando banco no existe")
    void testCreate_WithNonExistentBank_ThrowsException() {
        // Arrange
        when(bankService.findById(1L, enterpriseId)).thenThrow(new RuntimeException("Bank not found"));

        // Act & Assert
        assertThrows(BankNotFoundForAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando banco está inactivo")
    void testCreate_WithInactiveBank_ThrowsException() {
        // Arrange
        bank.setStatus(false);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);

        // Act & Assert
        assertThrows(BankNotFoundForAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta contable es null")
    void testCreate_WithNullAccountingAccountId_ThrowsException() {
        // Arrange
        createRequest.setAccountingAccountId(null);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);

        // Act & Assert
        assertThrows(InvalidAccountingAccountForBankAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta contable no existe")
    void testCreate_WithNonExistentAccountingAccount_ThrowsException() {
        // Arrange
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(InvalidAccountingAccountForBankAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código de cuenta contable no tiene 8 dígitos")
    void testCreate_WithInvalidAccountingAccountCode_ThrowsException() {
        // Arrange
        accountingAccount.setCode("1105050"); // 7 dígitos
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);

        // Act & Assert
        assertThrows(InvalidAccountingAccountForBankAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código de cuenta contable contiene letras")
    void testCreate_WithNonNumericAccountingAccountCode_ThrowsException() {
        // Arrange
        accountingAccount.setCode("1105050A");
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);

        // Act & Assert
        assertThrows(InvalidAccountingAccountForBankAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando número de cuenta ya existe")
    void testCreate_WithDuplicateAccountNumber_ThrowsException() {
        // Arrange
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.existsByAccountNumberAndIdEnterprise(12345678L, enterpriseId)).thenReturn(true);

        // Act & Assert
        assertThrows(BankAccountAlreadyExistsException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear cuenta bancaria con número de 8 dígitos")
    void testCreate_With8DigitAccountNumber_Success() {
        // Arrange
        createRequest.setAccountNumber(10000000L);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.existsByAccountNumberAndIdEnterprise(10000000L, enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bankAccount);
        when(dataMapper.toEntity(any(BankAccount.class))).thenReturn(bankAccountEntity);
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        BankAccount result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankAccountEntity.class));
    }

    @Test
    @DisplayName("Debe crear cuenta bancaria con número de 16 dígitos")
    void testCreate_With16DigitAccountNumber_Success() {
        // Arrange
        createRequest.setAccountNumber(9999999999999999L);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.existsByAccountNumberAndIdEnterprise(9999999999999999L, enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bankAccount);
        when(dataMapper.toEntity(any(BankAccount.class))).thenReturn(bankAccountEntity);
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        BankAccount result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankAccountEntity.class));
    }

    @Test
    @DisplayName("Debe actualizar cuenta bancaria exitosamente")
    void testUpdateSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);

        // Act
        BankAccount result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankAccountEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar cuenta bancaria que no existe")
    void testUpdate_WithNonExistentBankAccount_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar cuenta bancaria en uso")
    void testUpdate_WithBankAccountInUse_ThrowsException() {
        // Arrange
        bankAccount.setUsageCount(5);
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act & Assert
        assertThrows(BankAccountInUseException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar cuenta bancaria cambiando número de cuenta")
    void testUpdate_WithDifferentAccountNumber_Success() {
        // Arrange
        updateRequest.setAccountNumber(87654321L);
        bankAccountEntity.setAccountNumber(12345678L);
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);
        when(repository.existsByAccountNumberAndIdEnterprise(87654321L, enterpriseId)).thenReturn(false);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);

        // Act
        BankAccount result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankAccountEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con número de cuenta duplicado")
    void testUpdate_WithDuplicateAccountNumber_ThrowsException() {
        // Arrange
        updateRequest.setAccountNumber(87654321L);
        bankAccountEntity.setAccountNumber(12345678L);
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);
        when(repository.existsByAccountNumberAndIdEnterprise(87654321L, enterpriseId)).thenReturn(true);

        // Act & Assert
        assertThrows(BankAccountAlreadyExistsException.class, () -> service.update(updateRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe buscar cuenta bancaria por ID exitosamente")
    void testFindByIdSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        BankAccount result = service.findById(1L, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).findByIdAndIdEnterprise(1L, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta bancaria no existe")
    void testFindById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () -> service.findById(1L, enterpriseId));
    }

    @Test
    @DisplayName("Debe obtener cuentas bancarias paginadas con búsqueda")
    void testFindAllByEnterpriseWithFilters_WithSearch_Success() {
        // Arrange
        Page<BankAccountEntity> page = new PageImpl<>(Collections.singletonList(bankAccountEntity));

        when(repository.countByIdEnterpriseAndAccountNumberSearch(enterpriseId, "12345")).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findByIdEnterpriseAndAccountNumberSearch(eq(enterpriseId), eq("12345"), any(Pageable.class)))
                .thenReturn(page);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        Page<BankAccount> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "accountNumber", "asc", "12345");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByIdEnterpriseAndAccountNumberSearch(eq(enterpriseId), eq("12345"), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener cuentas bancarias paginadas sin búsqueda")
    void testFindAllByEnterpriseWithFilters_WithoutSearch_Success() {
        // Arrange
        Page<BankAccountEntity> page = new PageImpl<>(Collections.singletonList(bankAccountEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        Page<BankAccount> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "accountNumber", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener cuentas bancarias con ordenamiento descendente")
    void testFindAllByEnterpriseWithFilters_WithDescOrder_Success() {
        // Arrange
        Page<BankAccountEntity> page = new PageImpl<>(Collections.singletonList(bankAccountEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        Page<BankAccount> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "accountNumber", "desc", null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("accountNumber").getDirection());
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
                service.findAllByEnterpriseWithFilters(enterpriseId, 0, 10, "accountNumber", "invalid", null)
        );
    }

    @Test
    @DisplayName("Debe obtener cuentas activas paginadas")
    void testFindAllActiveByEnterpriseSuccess() {
        // Arrange
        Page<BankAccountEntity> page = new PageImpl<>(Collections.singletonList(bankAccountEntity));

        when(repository.countByIdEnterpriseAndStatus(enterpriseId, true)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterpriseAndStatus(eq(enterpriseId), eq(true), any(Pageable.class)))
                .thenReturn(page);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        Page<BankAccount> result = service.findAllActiveByEnterprise(enterpriseId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByIdEnterpriseAndStatus(eq(enterpriseId), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe cambiar estado de cuenta bancaria exitosamente")
    void testChangeStateSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        BankAccount result = service.changeState(1L, enterpriseId, false);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<BankAccountEntity> entityCaptor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(repository).save(entityCaptor.capture());
        assertEquals(false, entityCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado de cuenta no existente")
    void testChangeState_WithNonExistentBankAccount_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () -> service.changeState(1L, enterpriseId, false));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar cuenta bancaria exitosamente")
    void testDeleteSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        BankAccount result = service.delete(1L, enterpriseId);

        // Assert
        assertNotNull(result);
        verify(repository).delete(bankAccountEntity);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar cuenta bancaria que no existe")
    void testDelete_WithNonExistentBankAccount_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () -> service.delete(1L, enterpriseId));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar cuenta bancaria en uso")
    void testDelete_WithBankAccountInUse_ThrowsException() {
        // Arrange
        bankAccount.setUsageCount(3);
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act & Assert
        assertThrows(BankAccountInUseException.class, () -> service.delete(1L, enterpriseId));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe actualizar contador de uso exitosamente")
    void testUpdateUsageCountSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);

        // Act
        service.updateUsageCount(1L, enterpriseId, 5);

        // Assert
        ArgumentCaptor<BankAccountEntity> entityCaptor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(repository).save(entityCaptor.capture());
        assertEquals(5, entityCaptor.getValue().getUsageCount());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar uso de cuenta no existente")
    void testUpdateUsageCount_WithNonExistentBankAccount_ThrowsException() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () -> service.updateUsageCount(1L, enterpriseId, 5));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar contador de uso a cero")
    void testUpdateUsageCount_ToZero_Success() {
        // Arrange
        bankAccountEntity.setUsageCount(10);
        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);

        // Act
        service.updateUsageCount(1L, enterpriseId, 0);

        // Assert
        ArgumentCaptor<BankAccountEntity> entityCaptor = ArgumentCaptor.forClass(BankAccountEntity.class);
        verify(repository).save(entityCaptor.capture());
        assertEquals(0, entityCaptor.getValue().getUsageCount());
    }

    @Test
    @DisplayName("Debe validar que cuenta contable con código vacío lanza excepción")
    void testCreate_WithEmptyAccountingAccountCode_ThrowsException() {
        // Arrange
        accountingAccount.setCode("");
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);

        // Act & Assert
        assertThrows(InvalidAccountingAccountForBankAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear cuenta con tipo CORRIENTE exitosamente")
    void testCreate_WithCorrienteAccountType_Success() {
        // Arrange
        createRequest.setAccountType(AccountType.CORRIENTE);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.existsByAccountNumberAndIdEnterprise(12345678L, enterpriseId)).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(bankAccount);
        when(dataMapper.toEntity(any(BankAccount.class))).thenReturn(bankAccountEntity);
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        BankAccount result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankAccountEntity.class));
    }

    @Test
    @DisplayName("Debe usar campo de ordenamiento por defecto cuando es null")
    void testFindAllByEnterpriseWithFilters_WithNullSortField_UsesDefault() {
        // Arrange
        Page<BankAccountEntity> page = new PageImpl<>(Collections.singletonList(bankAccountEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        Page<BankAccount> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, null, "asc", null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertNotNull(pageableCaptor.getValue().getSort().getOrderFor("accountNumber"));
    }

    @Test
    @DisplayName("Debe usar ordenamiento ascendente por defecto cuando sortOrder es null")
    void testFindAllByEnterpriseWithFilters_WithNullSortOrder_UsesAscending() {
        // Arrange
        Page<BankAccountEntity> page = new PageImpl<>(Collections.singletonList(bankAccountEntity));

        when(repository.countByIdEnterprise(enterpriseId)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L)))
                .thenReturn(PageRequest.of(0, 10));
        when(repository.findAllByIdEnterprise(eq(enterpriseId), any(Pageable.class))).thenReturn(page);
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);

        // Act
        Page<BankAccount> result = service.findAllByEnterpriseWithFilters(
                enterpriseId, 0, 10, "accountNumber", null, null);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdEnterprise(eq(enterpriseId), pageableCaptor.capture());
        assertEquals(Sort.Direction.ASC, pageableCaptor.getValue().getSort().getOrderFor("accountNumber").getDirection());
    }

    @Test
    @DisplayName("Debe actualizar cuenta bancaria sin cambiar banco cuando es el mismo")
    void testUpdate_WithSameBank_DoesNotUpdateBank() {
        // Arrange
        BankEntity existingBank = new BankEntity();
        existingBank.setId(1L);
        bankAccountEntity.setBank(existingBank);

        when(repository.findByIdAndIdEnterprise(1L, enterpriseId)).thenReturn(Optional.of(bankAccountEntity));
        when(dataMapper.toDomain(bankAccountEntity)).thenReturn(bankAccount);
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenReturn(accountingAccount);
        when(repository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);

        // Act
        BankAccount result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(BankAccountEntity.class));
    }

    @Test
    @DisplayName("Debe capturar excepción genérica del servicio de bancos y lanzar BankNotFoundForAccountException")
    void testCreate_WhenBankServiceThrowsGenericException_ThrowsBankNotFoundException() {
        // Arrange
        when(bankService.findById(1L, enterpriseId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(BankNotFoundForAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe capturar excepción genérica del servicio de cuentas contables y lanzar InvalidAccountingAccountForBankAccountException")
    void testCreate_WhenAccountCatalogueServiceThrowsException_ThrowsInvalidAccountingAccountException() {
        // Arrange
        when(bankService.findById(1L, enterpriseId)).thenReturn(bank);
        when(accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(1L, enterpriseId))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(InvalidAccountingAccountForBankAccountException.class, () -> service.create(createRequest));
        verify(repository, never()).save(any());
    }
}
