package co.unicauca.edu.co.contables.accounting.catalogue.unitAudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.audit.AuditAspectBankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuditAspectBanksAccountTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Mock
    private IBankAccountService bankAccountService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private TestableAuditAspectBankAccount aspect;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectBankAccount(
                auditEventBuilder,
                auditEventPublisher,
                bankAccountService);
    }

    // MEtodo fetchcurrentstae
    @Test
    @DisplayName("fetchCurrentState - tabla inválida retorna null")
    void fetchCurrentState_tablaInvalida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "OTHER");
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
        assertNull(result);
    }

    @Test
    @DisplayName("fetchCurrentState - obtiene datos usando Long/String")
    void fetchCurrentState_longArgs() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK_ACCOUNT");
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setIdEnterprise("ENT-1");
        account.setAccountNumber(12345L);
        when(bankAccountService.findById(1L, "ENT-1")).thenReturn(account);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        verify(bankAccountService).findById(1L, "ENT-1");
    }

    @Test
    @DisplayName("fetchCurrentState - obtiene datos usando request")
    void fetchCurrentState_request() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK_ACCOUNT");
        BankAccountUpdateReq request = mock(BankAccountUpdateReq.class);
        when(request.getId()).thenReturn(10L);
        when(request.getIdEnterprise()).thenReturn("ENT-REQ");
        BankAccount account = new BankAccount();
        account.setId(10L);
        when(bankAccountService.findById(10L, "ENT-REQ")).thenReturn(account);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { request });
        assertNotNull(result);
        verify(bankAccountService).findById(10L, "ENT-REQ");
    }

    @Test
    @DisplayName("fetchCurrentState - servicio null retorna null")
    void fetchCurrentState_serviceNull() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK_ACCOUNT");
        when(bankAccountService.findById(anyLong(), anyString())).thenReturn(null);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
        assertNull(result);
    }

    // Metodo resolveEnterpriseId
    @Test
    @DisplayName("resolveEnterpriseId - CREATE obtiene enterprise desde result")
    void resolveEnterpriseId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK_ACCOUNT");
        BankAccount account = new BankAccount();
        account.setIdEnterprise("ENT-1");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, account, null);
        assertEquals("ENT-1", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - CREATE inválido retorna UNKNOWN")
    void resolveEnterpriseId_createInvalid() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK_ACCOUNT");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, new Object(), null);
        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE obtiene enterprise desde args")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "BANK_ACCOUNT");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] { 1L, "ENT-DELETE" }, null, null);
        assertEquals("ENT-DELETE", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - INACTIVATE obtiene enterprise desde args")
    void resolveEnterpriseId_inactivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE, "BANK_ACCOUNT");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] { 1L, "ENT-INACT" }, null, null);
        assertEquals("ENT-INACT", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - ACTIVATE obtiene enterprise desde args")
    void resolveEnterpriseId_activate() {
        Auditable auditable = mockAuditable(OperationType.ACTIVATE, "BANK_ACCOUNT");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] { 1L, "ENT-ACT" }, null, null);
        assertEquals("ENT-ACT", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE obtiene entId desde beforeData")
    void resolveEnterpriseId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK_ACCOUNT");
        Map<String, Object> before = Map.of("entId", "ENT-UPD");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
        assertEquals("ENT-UPD", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE con beforeData null retorna UNKNOWN")
    void resolveEnterpriseId_updateNull() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK_ACCOUNT");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", result);
    }

    // Metodo resolveRegisterId
    @Test
    @DisplayName("resolveRegisterId - CREATE obtiene id desde result")
    void resolveRegisterId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK_ACCOUNT");
        BankAccount account = new BankAccount();
        account.setId(99L);
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, account, null);
        assertEquals("99", result);
    }

    @Test
    @DisplayName("resolveRegisterId - CREATE inválido retorna UNKNOWN")
    void resolveRegisterId_createInvalid() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK_ACCOUNT");
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, new Object(), null);
        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveRegisterId - DELETE obtiene id desde args")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "BANK_ACCOUNT");
        when(auditable.idArgIndex()).thenReturn(0);
        String result = aspect.testResolveRegisterId(auditable, new Object[] { 55L, "ENT-1" }, null, null);
        assertEquals("55", result);
    }

    @Test
    @DisplayName("resolveRegisterId - INACTIVATE obtiene id desde args")
    void resolveRegisterId_inactivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE, "BANK_ACCOUNT");
        when(auditable.idArgIndex()).thenReturn(0);
        String result = aspect.testResolveRegisterId(auditable, new Object[] { 66L, "ENT-1" }, null, null);
        assertEquals("66", result);
    }

    @Test
    @DisplayName("resolveRegisterId - ACTIVATE obtiene id desde args")
    void resolveRegisterId_activate() {
        Auditable auditable = mockAuditable(OperationType.ACTIVATE, "BANK_ACCOUNT");
        when(auditable.idArgIndex()).thenReturn(0);
        String result = aspect.testResolveRegisterId(auditable, new Object[] { 77L, "ENT-1" }, null, null);
        assertEquals("77", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE obtiene id desde beforeData")
    void resolveRegisterId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK_ACCOUNT");
        Map<String, Object> before = Map.of("id", 88L);
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
        assertEquals("88", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE con beforeData null retorna UNKNOWN")
    void resolveRegisterId_updateNull() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK_ACCOUNT");
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", result);
    }

    // Metodo buildcontext
    @Test
    @DisplayName("buildContext - beforeData null retorna vacío")
    void buildContext_null() {
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("buildContext - incluye accountNumber y accountType")
    void buildContext_complete() {
        Map<String, Object> before = Map.of("accountNumber", "123456", "accountType", "SAVINGS");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
        assertEquals("123456", result.get("accountNumber"));
        assertEquals("SAVINGS", result.get("accountType"));
    }

    @Test
    @DisplayName("buildContext - incluye solo accountNumber")
    void buildContext_onlyAccountNumber() {
        Map<String, Object> before = Map.of("accountNumber", "123456");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
        assertEquals("123456", result.get("accountNumber"));
        assertFalse(result.containsKey("accountType"));
    }

    @Test
    @DisplayName("buildContext - incluye solo accountType")
    void buildContext_onlyAccountType() {
        Map<String, Object> before = Map.of("accountType", "CHECKING");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
        assertEquals("CHECKING", result.get("accountType"));
        assertFalse(result.containsKey("accountNumber"));
    }

    // Entity to map
    @Test
    @DisplayName("entityToMap - objeto inválido retorna null")
    void entityToMap_invalid() {
        Map<String, Object> result = aspect.testEntityToMap(new Object());
        assertNull(result);
    }

    @Test
    @DisplayName("entityToMap - mapea correctamente")
    void entityToMap_complete() {
        Bank bank = new Bank();
        bank.setCode("BAN001");
        bank.setName("Bancolombia");
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setIdEnterprise("ENT-1");
        account.setAccountNumber(123456L);
        account.setBank(bank);
        account.setAccountType(AccountType.AHORROS);
        account.setAccountingAccountId(99L);
        account.setStatus(true);
        Map<String, Object> result = aspect.testEntityToMap(account);
        assertEquals(1L, result.get("id"));
        assertEquals("ENT-1", result.get("entId"));
        assertEquals(123456L, result.get("accountNumber"));
        assertEquals("BAN001 - Bancolombia", result.get("bank"));
        assertEquals("AHORROS", result.get("accountType"));
        assertEquals(99L, result.get("accountingAccountId"));
        assertEquals(true, result.get("state"));
    }

    @Test
    @DisplayName("entityToMap - bank null no agrega campo bank")
    void entityToMap_bankNull() {
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setAccountNumber(123L);
        Map<String, Object> result = aspect.testEntityToMap(account);
        assertFalse(result.containsKey("bank"));
    }

    @Test
    @DisplayName("entityToMap - accountType null no agrega campo")
    void entityToMap_accountTypeNull() {
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setAccountNumber(123L);
        Map<String, Object> result = aspect.testEntityToMap(account);
        assertFalse(result.containsKey("accountType"));
    }

    @Test
    @DisplayName("entityToMap - elimina campos null")
    void entityToMap_removeNulls() {
        BankAccount account = new BankAccount();
        account.setId(1L);
        Map<String, Object> result = aspect.testEntityToMap(account);
        assertFalse(result.containsKey("accountNumber"));
        assertFalse(result.containsKey("bank"));
        assertFalse(result.containsKey("accountType"));
        assertFalse(result.containsKey("entId"));
    }

    @Test
    @DisplayName("audit - delega en executeAudit")
    void audit_ok() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK_ACCOUNT");
        when(joinPoint.getArgs()).thenReturn(new Object[] {});
        when(joinPoint.proceed()).thenReturn("OK");
        Object result = aspect.audit(joinPoint, auditable);
        assertEquals("OK", result);
    }

    // Helper
    private Auditable mockAuditable(OperationType operationType, String affectedTable) {
        Auditable auditable = mock(Auditable.class);
        lenient().when(auditable.operationType()).thenReturn(operationType);
        lenient().when(auditable.affectedTable()).thenReturn(affectedTable);
        return auditable;
    }

    private static class TestableAuditAspectBankAccount extends AuditAspectBankAccount {

        public TestableAuditAspectBankAccount(AuditEventBuilder auditEventBuilder,
                AuditEventPublisher auditEventPublisher, IBankAccountService bankAccountService) {
            super(auditEventBuilder, auditEventPublisher, bankAccountService);
        }

        public Map<String, Object> testFetchCurrentState(Auditable auditable, Object[] args) {
            return fetchCurrentState(auditable, args);
        }

        public String testResolveEnterpriseId(Auditable auditable, Object[] args, Object result,
                Map<String, Object> beforeData) {
            return resolveEnterpriseId(auditable, args, result, beforeData);
        }

        public String testResolveRegisterId(Auditable auditable, Object[] args, Object result,
                Map<String, Object> beforeData) {
            return resolveRegisterId(auditable, args, result, beforeData);
        }

        public Map<String, Object> testBuildContext(Object[] args, Object result, Map<String, Object> beforeData) {
            return buildContext(args, result, beforeData);
        }

        public Map<String, Object> testEntityToMap(Object object) {
            return entityToMap(object);
        }
    }
}
