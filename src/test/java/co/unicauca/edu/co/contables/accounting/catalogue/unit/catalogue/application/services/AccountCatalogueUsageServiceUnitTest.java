package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueUsageService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.AccountUsedEventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueUsageServiceUnitTest {

    @Mock
    private IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

    @Mock
    private IAccountCatalogueUpdateOutputPort accountCatalogueUpdateOutputPort;

    @InjectMocks
    private AccountCatalogueUsageService usageService;

    private AccountUsedEventDto eventDto;
    private AccountCatalogue accountCatalogue;
    private AccountCatalogue updatedAccount;
    private static final Long ACCOUNT_ID = 1L;
    private static final Long ACCOUNT_CODE = 11050101L;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final String TYPE_ID = "ID";
    private static final String TYPE_CODE = "CODE";

    @BeforeEach
    void setUp() {
        eventDto = new AccountUsedEventDto();
        eventDto.setEnterpriseId(ENTERPRISE_ID);

        accountCatalogue = new AccountCatalogue();
        accountCatalogue.setId(ACCOUNT_ID);
        accountCatalogue.setCode("11050101");
        accountCatalogue.setDescription("Caja General");
        accountCatalogue.setUsageCount(5);
        accountCatalogue.setIdEnterprise(ENTERPRISE_ID);

        updatedAccount = new AccountCatalogue();
        updatedAccount.setId(ACCOUNT_ID);
        updatedAccount.setCode("11050101");
        updatedAccount.setDescription("Caja General");
        updatedAccount.setUsageCount(6);
        updatedAccount.setIdEnterprise(ENTERPRISE_ID);
    }

    // ========== Tests de búsqueda por ID ==========

    @Test
    @DisplayName("Debe incrementar contador de uso cuando se busca por ID")
    void testIncrementUsageCountByIdSuccessfully() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);
        verify(accountCatalogueUpdateOutputPort).incrementUsageCount(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Debe buscar cuenta por ID cuando sourceAccountType es ID en mayúsculas")
    void testIncrementUsageCountByIdUppercase() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("ID");
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);
        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueByCode(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe buscar cuenta por ID cuando sourceAccountType es id en minúsculas")
    void testIncrementUsageCountByIdLowercase() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("id");
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe buscar cuenta por ID cuando sourceAccountType es Id mixto")
    void testIncrementUsageCountByIdMixedCase() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("Id");
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);
    }

    // ========== Tests de búsqueda por CODE ==========

    @Test
    @DisplayName("Debe incrementar contador de uso cuando se busca por CODE")
    void testIncrementUsageCountByCodeSuccessfully() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType(TYPE_CODE);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID);
        verify(accountCatalogueUpdateOutputPort).incrementUsageCount(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Debe buscar cuenta por CODE cuando sourceAccountType es CODE en mayúsculas")
    void testIncrementUsageCountByCodeUppercase() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType("CODE");
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID);
        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueById(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe buscar cuenta por CODE cuando sourceAccountType es code en minúsculas")
    void testIncrementUsageCountByCodeLowercase() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType("code");
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe buscar cuenta por CODE cuando sourceAccountType es Code mixto")
    void testIncrementUsageCountByCodeMixedCase() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType("Code");
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID);
    }

    // ========== Tests de tipo inválido ==========

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando sourceAccountType es inválido")
    void testIncrementUsageCountThrowsExceptionForInvalidType() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("INVALID");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usageService.incrementUsageCount(eventDto));
        
        assertTrue(exception.getMessage().contains("Tipo de fuente de cuenta inválido"));
        assertTrue(exception.getMessage().contains("INVALID"));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando sourceAccountType es null")
    void testIncrementUsageCountThrowsExceptionForNullType() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> usageService.incrementUsageCount(eventDto));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando sourceAccountType es vacío")
    void testIncrementUsageCountThrowsExceptionForEmptyType() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> usageService.incrementUsageCount(eventDto));
    }

    @Test
    @DisplayName("Debe incluir los tipos válidos en el mensaje de error")
    void testIncrementUsageCountErrorMessageContainsValidTypes() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("NUMERO");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usageService.incrementUsageCount(eventDto));
        
        assertTrue(exception.getMessage().contains("ID"));
        assertTrue(exception.getMessage().contains("CODE"));
    }

    // ========== Tests de cuenta no encontrada ==========

    @Test
    @DisplayName("Debe lanzar AccountCatalogueNotFoundException cuando cuenta por ID no existe")
    void testIncrementUsageCountThrowsNotFoundExceptionForIdSearch() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> usageService.incrementUsageCount(eventDto));
        
        assertTrue(exception.getMessage().contains(ACCOUNT_ID.toString()));
    }

    @Test
    @DisplayName("Debe lanzar AccountCatalogueNotFoundException cuando cuenta por CODE no existe")
    void testIncrementUsageCountThrowsNotFoundExceptionForCodeSearch() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType(TYPE_CODE);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> usageService.incrementUsageCount(eventDto));
        
        assertTrue(exception.getMessage().contains(ACCOUNT_CODE.toString()));
    }

    @Test
    @DisplayName("Debe incluir el tipo de búsqueda en el mensaje de error cuando no encuentra por ID")
    void testNotFoundExceptionContainsTypeForIdSearch() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> usageService.incrementUsageCount(eventDto));
        
        assertTrue(exception.getMessage().contains(TYPE_ID));
    }

    @Test
    @DisplayName("Debe incluir el tipo de búsqueda en el mensaje de error cuando no encuentra por CODE")
    void testNotFoundExceptionContainsTypeForCodeSearch() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType(TYPE_CODE);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> usageService.incrementUsageCount(eventDto));
        
        assertTrue(exception.getMessage().contains(TYPE_CODE));
    }

    // ========== Tests de no invocación de incremento ==========

    @Test
    @DisplayName("No debe invocar incrementUsageCount cuando la cuenta no se encuentra por ID")
    void testDoesNotIncrementWhenAccountNotFoundById() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(null);

        // Act
        try {
            usageService.incrementUsageCount(eventDto);
        } catch (AccountCatalogueNotFoundException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueUpdateOutputPort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("No debe invocar incrementUsageCount cuando la cuenta no se encuentra por CODE")
    void testDoesNotIncrementWhenAccountNotFoundByCode() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType(TYPE_CODE);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(null);

        // Act
        try {
            usageService.incrementUsageCount(eventDto);
        } catch (AccountCatalogueNotFoundException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueUpdateOutputPort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("No debe invocar incrementUsageCount cuando el tipo es inválido")
    void testDoesNotIncrementWhenTypeIsInvalid() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("INVALID");

        // Act
        try {
            usageService.incrementUsageCount(eventDto);
        } catch (IllegalArgumentException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueUpdateOutputPort, never()).incrementUsageCount(anyLong());
        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueById(anyLong(), anyString());
        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueByCode(anyString(), anyString());
    }

    // ========== Tests de incrementUsageCount con retorno null ==========

    @Test
    @DisplayName("Debe completar sin errores cuando incrementUsageCount retorna null")
    void testIncrementUsageCountCompletesWhenUpdateReturnsNull() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> usageService.incrementUsageCount(eventDto));
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount aunque retorne null")
    void testIncrementUsageCountIsInvokedEvenWhenReturnsNull() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(null);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueUpdateOutputPort).incrementUsageCount(ACCOUNT_ID);
    }

    // ========== Tests de conversión de código ==========

    @Test
    @DisplayName("Debe convertir el account Long a String cuando busca por código")
    void testConvertsAccountToStringWhenSearchingByCode() {
        // Arrange
        Long codeAsLong = 12345678L;
        eventDto.setAccount(codeAsLong);
        eventDto.setSourceAccountType(TYPE_CODE);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("12345678", ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByCode("12345678", ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe usar el ID de la cuenta encontrada para incrementar, no el parámetro")
    void testUsesFoundAccountIdForIncrement() {
        // Arrange
        Long codeAsLong = 99999999L;
        eventDto.setAccount(codeAsLong);
        eventDto.setSourceAccountType(TYPE_CODE);
        
        AccountCatalogue foundAccount = new AccountCatalogue();
        foundAccount.setId(500L);
        foundAccount.setCode("99999999");
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("99999999", ENTERPRISE_ID))
                .thenReturn(foundAccount);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(500L)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueUpdateOutputPort).incrementUsageCount(500L);
    }

    // ========== Tests de flujo completo ==========

    @Test
    @DisplayName("Debe ejecutar flujo completo correctamente para búsqueda por ID")
    void testFullFlowForIdSearch() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort, times(1)).getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);
        verify(accountCatalogueUpdateOutputPort, times(1)).incrementUsageCount(ACCOUNT_ID);
        verifyNoMoreInteractions(accountCatalogueSearchOutputPort);
        verifyNoMoreInteractions(accountCatalogueUpdateOutputPort);
    }

    @Test
    @DisplayName("Debe ejecutar flujo completo correctamente para búsqueda por CODE")
    void testFullFlowForCodeSearch() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType(TYPE_CODE);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort, times(1)).getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID);
        verify(accountCatalogueUpdateOutputPort, times(1)).incrementUsageCount(ACCOUNT_ID);
        verifyNoMoreInteractions(accountCatalogueSearchOutputPort);
        verifyNoMoreInteractions(accountCatalogueUpdateOutputPort);
    }

    // ========== Tests de diferentes valores de enterpriseId ==========

    @Test
    @DisplayName("Debe usar el enterpriseId del evento para la búsqueda por ID")
    void testUsesEnterpriseIdFromEventForIdSearch() {
        // Arrange
        String differentEnterpriseId = "ENT999";
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        eventDto.setEnterpriseId(differentEnterpriseId);
        
        AccountCatalogue foundAccount = new AccountCatalogue();
        foundAccount.setId(ACCOUNT_ID);
        foundAccount.setIdEnterprise(differentEnterpriseId);
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, differentEnterpriseId))
                .thenReturn(foundAccount);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(ACCOUNT_ID, differentEnterpriseId);
    }

    @Test
    @DisplayName("Debe usar el enterpriseId del evento para la búsqueda por CODE")
    void testUsesEnterpriseIdFromEventForCodeSearch() {
        // Arrange
        String differentEnterpriseId = "ENT888";
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType(TYPE_CODE);
        eventDto.setEnterpriseId(differentEnterpriseId);
        
        AccountCatalogue foundAccount = new AccountCatalogue();
        foundAccount.setId(ACCOUNT_ID);
        foundAccount.setIdEnterprise(differentEnterpriseId);
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), differentEnterpriseId))
                .thenReturn(foundAccount);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByCode(ACCOUNT_CODE.toString(), differentEnterpriseId);
    }

    // ========== Tests de valores de account ==========

    @Test
    @DisplayName("Debe funcionar con account igual a 0")
    void testIncrementUsageCountWithZeroAccount() {
        // Arrange
        eventDto.setAccount(0L);
        eventDto.setSourceAccountType(TYPE_ID);
        
        AccountCatalogue foundAccount = new AccountCatalogue();
        foundAccount.setId(0L);
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(0L, ENTERPRISE_ID))
                .thenReturn(foundAccount);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(0L)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(0L, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe funcionar con account de valor grande")
    void testIncrementUsageCountWithLargeAccountValue() {
        // Arrange
        Long largeAccountId = 9999999999L;
        eventDto.setAccount(largeAccountId);
        eventDto.setSourceAccountType(TYPE_ID);
        
        AccountCatalogue foundAccount = new AccountCatalogue();
        foundAccount.setId(largeAccountId);
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(largeAccountId, ENTERPRISE_ID))
                .thenReturn(foundAccount);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(largeAccountId)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(largeAccountId, ENTERPRISE_ID);
        verify(accountCatalogueUpdateOutputPort).incrementUsageCount(largeAccountId);
    }

    // ========== Tests de orden de ejecución ==========

    @Test
    @DisplayName("Debe buscar la cuenta antes de incrementar el contador")
    void testSearchesBeforeIncrementing() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(TYPE_ID);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        var inOrder = inOrder(accountCatalogueSearchOutputPort, accountCatalogueUpdateOutputPort);
        inOrder.verify(accountCatalogueSearchOutputPort).getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);
        inOrder.verify(accountCatalogueUpdateOutputPort).incrementUsageCount(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Debe validar existencia antes de incrementar cuando busca por CODE")
    void testValidatesExistenceBeforeIncrementingByCode() {
        // Arrange
        eventDto.setAccount(ACCOUNT_CODE);
        eventDto.setSourceAccountType(TYPE_CODE);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        // Act
        usageService.incrementUsageCount(eventDto);

        // Assert
        var inOrder = inOrder(accountCatalogueSearchOutputPort, accountCatalogueUpdateOutputPort);
        inOrder.verify(accountCatalogueSearchOutputPort).getAccountCatalogueByCode(ACCOUNT_CODE.toString(), ENTERPRISE_ID);
        inOrder.verify(accountCatalogueUpdateOutputPort).incrementUsageCount(ACCOUNT_ID);
    }

    // ========== Tests de tipos adicionales inválidos ==========

    @Test
    @DisplayName("Debe lanzar excepción para tipo con espacios")
    void testThrowsExceptionForTypeWithSpaces() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType(" ID ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> usageService.incrementUsageCount(eventDto));
    }

    @Test
    @DisplayName("Debe lanzar excepción para tipo NUMBER")
    void testThrowsExceptionForTypeNumber() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("NUMBER");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> usageService.incrementUsageCount(eventDto));
    }

    @Test
    @DisplayName("Debe lanzar excepción para tipo NAME")
    void testThrowsExceptionForTypeName() {
        // Arrange
        eventDto.setAccount(ACCOUNT_ID);
        eventDto.setSourceAccountType("NAME");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> usageService.incrementUsageCount(eventDto));
    }

    // ========== Tests de múltiples llamadas ==========

    @Test
    @DisplayName("Debe poder procesar múltiples eventos consecutivos")
    void testCanProcessMultipleConsecutiveEvents() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(anyLong(), eq(ENTERPRISE_ID)))
                .thenReturn(accountCatalogue);
        when(accountCatalogueUpdateOutputPort.incrementUsageCount(ACCOUNT_ID)).thenReturn(updatedAccount);

        AccountUsedEventDto event1 = new AccountUsedEventDto(1L, ENTERPRISE_ID, TYPE_ID);
        AccountUsedEventDto event2 = new AccountUsedEventDto(2L, ENTERPRISE_ID, TYPE_ID);
        AccountUsedEventDto event3 = new AccountUsedEventDto(3L, ENTERPRISE_ID, TYPE_ID);

        // Act
        usageService.incrementUsageCount(event1);
        usageService.incrementUsageCount(event2);
        usageService.incrementUsageCount(event3);

        // Assert
        verify(accountCatalogueSearchOutputPort, times(3)).getAccountCatalogueById(anyLong(), eq(ENTERPRISE_ID));
        verify(accountCatalogueUpdateOutputPort, times(3)).incrementUsageCount(ACCOUNT_ID);
    }
}
