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
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.audit.AuditAspectBank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.IBankService;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuditAspectBanksTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Mock
    private IBankService bankService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private TestableAuditAspectBank aspect;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectBank(
                auditEventBuilder,
                auditEventPublisher,
                bankService);
    }

    // Metodo fetchCurrentState
    @Test
    @DisplayName("fetchCurrentState - tabla inválida retorna null")
    void fetchCurrentState_tablaInvalida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "OTHER");
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
        assertNull(result);
    }

    @Test
    @DisplayName("fetchCurrentState - debe obtener datos usando argumentos Long/String")
    void fetchCurrentState_argumentosLong() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK");
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setIdEnterprise("ENT-1");
        when(bankService.findById(1L, "ENT-1")).thenReturn(bank);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        verify(bankService).findById(1L, "ENT-1");
    }

    @Test
    @DisplayName("fetchCurrentState - debe obtener datos usando BankUpdateReq")
    void fetchCurrentState_request() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK");
        BankUpdateReq request = mock(BankUpdateReq.class);
        when(request.getId()).thenReturn(1L);
        when(request.getIdEnterprise()).thenReturn("ENT-1");
        Bank bank = new Bank();
        bank.setId(1L);
        when(bankService.findById(1L, "ENT-1")).thenReturn(bank);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { request });
        assertNotNull(result);
        verify(bankService).findById(1L, "ENT-1");
    }

    @Test
    @DisplayName("fetchCurrentState - servicio null retorna null")
    void fetchCurrentState_servicioNull() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK");
        when(bankService.findById(anyLong(), anyString())).thenReturn(null);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
        assertNull(result);
    }

    // Metodo resolveEnterpriseId
    @Test
    @DisplayName("resolveEnterpriseId - CREATE debe obtener enterprise desde result")
    void resolveEnterpriseId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK");
        Bank bank = new Bank();
        bank.setIdEnterprise("ENT-1");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, bank, null);
        assertEquals("ENT-1", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE debe obtener enterprise desde args")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "BANK");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] { 1L, "ENT-DELETE" }, null, null);
        assertEquals("ENT-DELETE", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - CREATE inválido retorna UNKNOWN")
    void resolveEnterpriseId_createInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, new Object(), null);
        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE obtiene entId desde beforeData")
    void resolveEnterpriseId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK");
        Map<String, Object> before = Map.of(
                "entId", "ENT-UPD");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
        assertEquals("ENT-UPD", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - INACTIVATE obtiene enterprise desde args")
    void resolveEnterpriseId_inactivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE, "BANK");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] { 1L, "ENT-INACT" }, null, null);
        assertEquals("ENT-INACT", result);
    }

    // Metodo resolveRegisterId
    @Test
    @DisplayName("resolveRegisterId - DELETE debe obtener id desde args")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "BANK");
        when(auditable.idArgIndex()).thenReturn(0);
        String result = aspect.testResolveRegisterId(auditable, new Object[] { 55L, "ENT-1" }, null, null);
        assertEquals("55", result);
    }

    @Test
    @DisplayName("resolveRegisterId - CREATE obtiene id desde result")
    void resolveRegisterId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK");
        Bank bank = new Bank();
        bank.setId(99L);
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, bank, null);
        assertEquals("99", result);
    }

    @Test
    @DisplayName("resolveRegisterId - CREATE inválido retorna UNKNOWN")
    void resolveRegisterId_createInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK");
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, new Object(), null);
        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE obtiene id desde beforeData")
    void resolveRegisterId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK");
        Map<String, Object> before = Map.of("id", 77L);
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
        assertEquals("77", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE con beforeData null retorna UNKNOWN")
    void resolveRegisterId_updateNull() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "BANK");
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveRegisterId - INACTIVATE obtiene id desde args")
    void resolveRegisterId_inactivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE, "BANK");
        when(auditable.idArgIndex()).thenReturn(0);
        String result = aspect.testResolveRegisterId(auditable, new Object[] { 45L, "ENT-1" }, null, null);
        assertEquals("45", result);
    }

    // metodo buildContext
    @Test
    @DisplayName("buildContext - debe incluir code y name")
    void buildContext_ok() {
        Map<String, Object> before = Map.of("code", "B001", "name", "Bancolombia");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
        assertEquals("B001", result.get("code"));
        assertEquals("Bancolombia", result.get("name"));
    }

    @Test
    @DisplayName("buildContext - beforeData null retorna vacío")
    void buildContext_null() {
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("buildContext - debe incluir solo code")
    void buildContext_soloCode() {
        Map<String, Object> before = Map.of("code", "B001");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
        assertEquals("B001", result.get("code"));
        assertFalse(result.containsKey("name"));
    }

    @Test
    @DisplayName("buildContext - debe incluir solo name")
    void buildContext_soloName() {
        Map<String, Object> before = Map.of("name", "Bancolombia");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
        assertEquals("Bancolombia", result.get("name"));
        assertFalse(result.containsKey("code"));
    }

    // metodo entityToMap
    @Test
    @DisplayName("entityToMap - objeto inválido retorna null")
    void entityToMap_invalido() {
        Map<String, Object> result = aspect.testEntityToMap(new Object());
        assertNull(result);
    }

    @Test
    @DisplayName("entityToMap - debe mapear correctamente")
    void entityToMap_ok() {
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setIdEnterprise("ENT-1");
        bank.setCode("B001");
        bank.setName("Bancolombia");
        bank.setStatus(true);
        Set<Currency> currencies = Set.of(Currency.COP, Currency.USD);
        bank.setCurrencies(currencies);
        Map<String, Object> result = aspect.testEntityToMap(bank);
        assertEquals(1L, result.get("id"));
        assertEquals("ENT-1", result.get("entId"));
        assertEquals("B001", result.get("code"));
        assertEquals("Bancolombia", result.get("name"));
        assertEquals(currencies, result.get("currencies"));
        assertEquals(true, result.get("state"));
    }

    @Test
    @DisplayName("entityToMap - debe eliminar campos null")
    void entityToMap_removeNulls() {
        Bank bank = new Bank();
        bank.setId(1L);
        Map<String, Object> result = aspect.testEntityToMap(bank);
        assertFalse(result.containsKey("name"));
        assertFalse(result.containsKey("code"));
        assertFalse(result.containsKey("currencies"));
    }

    @Test
    @DisplayName("entityToMap - elimina campos null manteniendo válidos")
    void entityToMap_partialNulls() {
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setCode("B001");
        Map<String, Object> result = aspect.testEntityToMap(bank);
        assertEquals("B001", result.get("code"));
        assertFalse(result.containsKey("name"));
        assertFalse(result.containsKey("currencies"));
        assertFalse(result.containsKey("entId"));
    }

    // Metodo audit
    @Test
    @DisplayName("audit - debe delegar en executeAudit")
    void audit_ok() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.CREATE, "BANK");
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

    private static class TestableAuditAspectBank extends AuditAspectBank {
        public TestableAuditAspectBank(AuditEventBuilder auditEventBuilder, AuditEventPublisher auditEventPublisher,
                IBankService bankService) {
            super(auditEventBuilder, auditEventPublisher, bankService);
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
