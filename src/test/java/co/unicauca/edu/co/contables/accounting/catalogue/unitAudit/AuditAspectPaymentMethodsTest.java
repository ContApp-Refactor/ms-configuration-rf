package co.unicauca.edu.co.contables.accounting.catalogue.unitAudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.audit.AuditAspectPayMethods;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;

@ExtendWith(MockitoExtension.class)
class AuditAspectPaymentMethodsTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Mock
    private IPaymentMethodService paymentMethodService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private TestableAuditAspectPayMethods aspect;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectPayMethods(
                auditEventBuilder,
                auditEventPublisher,
                paymentMethodService);
    }

    // Metodo fetchCurrentState
    @Test
    @DisplayName("fetchCurrentState - tabla no soportada retorna null")
    void fetchCurrentState_tablaInvalida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "OTHER", 0, 1);

        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });

        assertNull(result);
    }

    @Test
    @DisplayName("fetchCurrentState - PAYMENT_METHOD con id y enterpriseId como argumentos simples")
    void fetchCurrentState_conIdYEnterprise() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE,
                "PAYMENT_METHOD", 0, 1);
        AccountCatalogue account = new AccountCatalogue();
        account.setId(100L);
        PaymentMethod entity = new PaymentMethod();
        entity.setId(10L);
        entity.setIdEnterprise("ENT-2");
        entity.setName("TRANSFER");
        entity.setAccountingAccountEntity(account);
        when(paymentMethodService.findById(10L, "ENT-2"))
                .thenReturn(entity);
        Map<String, Object> result = aspect.testFetchCurrentState(
                auditable,
                new Object[] { 10L, "ENT-2" });
        assertNotNull(result);
        assertEquals(10L, result.get("id"));
        assertEquals("ENT-2", result.get("entId"));
        verify(paymentMethodService)
                .findById(10L, "ENT-2");
    }

    @Test
    @DisplayName("fetchCurrentState - PAYMENT_METHOD con PaymentMethodUpdateReq como primer argumento")
    void fetchCurrentState_conUpdateReq() {
        Auditable auditable = mockAuditable(OperationType.UPDATE,
                "PAYMENT_METHOD", 0, 1);
        PaymentMethodUpdateReq req = new PaymentMethodUpdateReq();
        req.setId(5L);
        req.setIdEnterprise("ENT-3");
        AccountCatalogue account = new AccountCatalogue();
        account.setId(200L);
        PaymentMethod entity = new PaymentMethod();
        entity.setId(5L);
        entity.setIdEnterprise("ENT-3");
        entity.setName("CARD");
        entity.setAccountingAccountEntity(account);
        when(paymentMethodService.findById(5L, "ENT-3"))
                .thenReturn(entity);
        Map<String, Object> result = aspect.testFetchCurrentState(
                auditable,
                new Object[] { req });
        assertNotNull(result);
        assertEquals(5L, result.get("id"));
        assertEquals("ENT-3", result.get("entId"));
        verify(paymentMethodService)
                .findById(5L, "ENT-3");
    }

    @Test
    @DisplayName("fetchCurrentState - servicio retorna null")
    void fetchCurrentState_serviceNull() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "PAYMENT_METHOD", 0, 1);
        PaymentMethodUpdateReq req = new PaymentMethodUpdateReq();
        req.setId(1L);
        req.setIdEnterprise("ENT-1");
        when(paymentMethodService.findById(1L, "ENT-1"))
                .thenReturn(null);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { req });
        assertNull(result);
    }

    // Resolve EnterpriseId
    @Test
    @DisplayName("resolveEnterpriseId - CREATE con PaymentMethod válido")
    void resolveEnterpriseId_createValido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "PAYMENT_METHOD", 0, 1);

        PaymentMethod pm = new PaymentMethod();
        pm.setIdEnterprise("ENT-1");

        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, pm, null);

        assertEquals("ENT-1", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - CREATE con resultado no PaymentMethod retorna UNKNOWN")
    void resolveEnterpriseId_createInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "PAYMENT_METHOD", 0, 1);

        Object notPm = new Object();

        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, notPm, null);

        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE usa enterpriseIdArgIndex de la anotación")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "PAYMENT_METHOD", 0, 1);

        Object[] args = { 5L, "ENT-3" };

        String result = aspect.testResolveEnterpriseId(auditable, args, null, null);

        assertEquals("ENT-3", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE con beforeData usa entId")
    void resolveEnterpriseId_updateConBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "PAYMENT_METHOD", 0, 1);

        Map<String, Object> beforeData = new HashMap<>();
        beforeData.put("entId", "ENT-4");

        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, beforeData);

        assertEquals("ENT-4", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveEnterpriseId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "PAYMENT_METHOD", 0, 1);

        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);

        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - INACTIVATE usa enterpriseIdArgIndex")
    void resolveEnterpriseId_inactivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE,
                "PAYMENT_METHOD", 0, 1);
        String result = aspect.testResolveEnterpriseId(
                auditable,
                new Object[] { 1L, "ENT-9" },
                null,
                null);
        assertEquals("ENT-9", result);
    }

    // Resolve RegisterId
    @Test
    @DisplayName("resolveRegisterId - CREATE con PaymentMethod válido")
    void resolveRegisterId_createValido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "PAYMENT_METHOD", 0, 1);

        PaymentMethod pm = new PaymentMethod();
        pm.setId(99L);

        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, pm, null);

        assertEquals("99", result);
    }

    @Test
    @DisplayName("resolveRegisterId - CREATE con resultado no PaymentMethod retorna UNKNOWN")
    void resolveRegisterId_createInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "PAYMENT_METHOD", 0, 1);

        Object notPm = new Object();

        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, notPm, null);

        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveRegisterId - DELETE usa idArgIndex de la anotación")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "PAYMENT_METHOD", 0, 1);

        Object[] args = { 7L, "ENT-3" };

        String result = aspect.testResolveRegisterId(auditable, args, null, null);

        assertEquals("7", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE con beforeData usa id")
    void resolveRegisterId_updateConBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "PAYMENT_METHOD", 0, 1);

        Map<String, Object> beforeData = new HashMap<>();
        beforeData.put("id", 123L);

        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, beforeData);

        assertEquals("123", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveRegisterId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "PAYMENT_METHOD", 0, 1);

        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);

        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveRegisterId - INACTIVATE usa idArgIndex")
    void resolveRegisterId_inactivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE,
                "PAYMENT_METHOD", 0, 1);
        String result = aspect.testResolveRegisterId(
                auditable,
                new Object[] { 99L, "ENT-1" },
                null,
                null);
        assertEquals("99", result);
    }

    // Metodo buildContext
    @Test
    @DisplayName("buildContext - beforeData null retorna mapa vacío")
    void buildContext_beforeDataNull() {
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("buildContext - agrega name cuando está presente")
    void buildContext_namePresente() {
        Map<String, Object> beforeData = new LinkedHashMap<>();
        beforeData.put("name", "TRANSFER");

        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, beforeData);

        assertEquals(1, result.size());
        assertEquals("TRANSFER", result.get("name"));
    }

    @Test
    @DisplayName("buildContext - sin name retorna mapa vacío")
    void buildContext_sinName() {
        Map<String, Object> beforeData = new LinkedHashMap<>();
        beforeData.put("other", "value");

        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, beforeData);

        assertTrue(result.isEmpty());
    }

    // Metodo entityToMap
    @Test
    @DisplayName("entityToMap - tipo inválido retorna null")
    void entityToMap_tipoInvalido() {
        Map<String, Object> result = aspect.testEntityToMap(new Object());

        assertNull(result);
    }

    @Test
    @DisplayName("entityToMap - mapea correctamente PaymentMethod y remueve nulos")
    void entityToMap_tipoValido() {
        AccountCatalogue accountEntity = new AccountCatalogue();
        accountEntity.setId(50L);

        PaymentMethod pm = new PaymentMethod();
        pm.setId(1L);
        pm.setIdEnterprise("ENT-1");
        pm.setName("CASH");
        pm.setAccountingAccountEntity(accountEntity);
        pm.setAccountingAccount("1105");
        pm.setStatus(true);

        Map<String, Object> result = aspect.testEntityToMap(pm);

        assertEquals(1L, result.get("id"));
        assertEquals("ENT-1", result.get("entId"));
        assertEquals("CASH", result.get("name"));
        assertEquals(50L, result.get("AccountCatalogueId"));
        assertEquals("1105", result.get("accountingAccount"));
        assertEquals(true, result.get("state"));
    }

    @Test
    @DisplayName("audit - debe delegar a executeAudit")
    void audit_delegaExecuteAudit() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.CREATE,
                "PAYMENT_METHOD", 0, 1);
        when(joinPoint.getArgs()).thenReturn(new Object[] {});
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.audit(joinPoint, auditable);

        assertEquals("OK", result);
    }

    // Helper
    private Auditable mockAuditable(OperationType operationType, String affectedTable, int idArgIndex,
            int enterpriseIdArgIndex) {
        Auditable auditable = mock(Auditable.class);
        lenient().when(auditable.operationType()).thenReturn(operationType);
        lenient().when(auditable.affectedTable()).thenReturn(affectedTable);
        lenient().when(auditable.idArgIndex()).thenReturn(idArgIndex);
        lenient().when(auditable.enterpriseIdArgIndex()).thenReturn(enterpriseIdArgIndex);
        return auditable;
    }

    private static class TestableAuditAspectPayMethods extends AuditAspectPayMethods {

        public TestableAuditAspectPayMethods(AuditEventBuilder auditEventBuilder,
                AuditEventPublisher auditEventPublisher,
                IPaymentMethodService paymentMethodService) {
            super(auditEventBuilder, auditEventPublisher, paymentMethodService);
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

        public Map<String, Object> testBuildContext(Object[] args, Object result,
                Map<String, Object> beforeData) {
            return buildContext(args, result, beforeData);
        }

        public Map<String, Object> testEntityToMap(Object object) {
            return entityToMap(object);
        }
    }
}
