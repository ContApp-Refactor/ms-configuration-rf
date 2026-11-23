package co.unicauca.edu.co.contables.accounting.catalogue.unitAudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.audit.AuditAspectTaxes;

@ExtendWith(MockitoExtension.class)
class AuditAspectTaxesTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Mock
    private ITaxSearchOutputPort taxSearchOutputPort;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private TestableAuditAspectTaxes aspect;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectTaxes(
                auditEventBuilder,
                auditEventPublisher,
                taxSearchOutputPort);
    }

    // Metodo fetch current state
    @Test
    @DisplayName("fetchCurrentState - tabla no soportada retorna null")
    void fetchCurrentState_tablaInvalida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "OTHER", 0, 1);
        Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
        assertNull(result);
    }

    @Test
    @DisplayName("fetchCurrentState - TAX con TaxDTO como primer argumento")
    void fetchCurrentState_conTaxDto() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TAX", 0, 1);
        TaxDTO dto = new TaxDTO();
        dto.setIdEnterprise("ENT-1");
        Tax entity = new Tax();
        entity.setId(1L);
        entity.setIdEnterprise("ENT-1");
        entity.setCode("T-001");
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(1L, "ENT-1"))
                .thenReturn(entity);
        Map<String, Object> result = aspect.testFetchCurrentState(
                auditable,
                new Object[] { dto, 1L });
        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals("ENT-1", result.get("entId"));
        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(1L, "ENT-1");
    }

    @Test
    @DisplayName("fetchCurrentState - TAX con id y enterpriseId como argumentos simples")
    void fetchCurrentState_conIdYEnterprise() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE, "TAX", 0, 1);
        Tax entity = new Tax();
        entity.setId(10L);
        entity.setIdEnterprise("ENT-2");
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(10L, "ENT-2"))
                .thenReturn(entity);
        Map<String, Object> result = aspect.testFetchCurrentState(
                auditable,
                new Object[] { 10L, "ENT-2" });
        assertNotNull(result);
        assertEquals(10L, result.get("id"));
        assertEquals("ENT-2", result.get("entId"));
        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(10L, "ENT-2");
    }

    // Metodo resolveEnterpriseId
    @Test
    @DisplayName("resolveEnterpriseId - CREATE con Tax válido")
    void resolveEnterpriseId_createValido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "TAX", 0, 1);
        Tax tax = new Tax();
        tax.setIdEnterprise("ENT-1");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, tax, null);
        assertEquals("ENT-1", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - CREATE con resultado no Tax retorna UNKNOWN")
    void resolveEnterpriseId_createInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "TAX", 0, 1);
        Object notTax = new Object();
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, notTax, null);
        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE usa enterpriseIdArgIndex de la anotación")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "TAX", 0, 1);
        Object[] args = { 5L, "ENT-3" };
        String result = aspect.testResolveEnterpriseId(auditable, args, null, null);
        assertEquals("ENT-3", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE con beforeData usa entId")
    void resolveEnterpriseId_updateConBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TAX", 0, 1);
        Map<String, Object> beforeData = new HashMap<>();
        beforeData.put("entId", "ENT-4");
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, beforeData);
        assertEquals("ENT-4", result);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveEnterpriseId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TAX", 0, 1);
        String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", result);
    }

    // metdodo resolveRegisterId
    @Test
    @DisplayName("resolveRegisterId - CREATE con Tax válido")
    void resolveRegisterId_createValido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "TAX", 0, 1);
        Tax tax = new Tax();
        tax.setId(99L);
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, tax, null);
        assertEquals("99", result);
    }

    @Test
    @DisplayName("resolveRegisterId - CREATE con resultado no Tax retorna UNKNOWN")
    void resolveRegisterId_createInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "TAX", 0, 1);
        Object notTax = new Object();
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, notTax, null);
        assertEquals("UNKNOWN", result);
    }

    @Test
    @DisplayName("resolveRegisterId - DELETE usa idArgIndex de la anotación")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "TAX", 0, 1);
        Object[] args = { 5L, "ENT-3" };
        String result = aspect.testResolveRegisterId(auditable, args, null, null);
        assertEquals("5", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE con beforeData usa id")
    void resolveRegisterId_updateConBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TAX", 0, 1);
        Map<String, Object> beforeData = new HashMap<>();
        beforeData.put("id", 123L);
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, beforeData);
        assertEquals("123", result);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveRegisterId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TAX", 0, 1);
        String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", result);
    }

    // metodo buildContext

    @Test
    @DisplayName("buildContext - beforeData null retorna mapa vacío")
    void buildContext_beforeDataNull() {
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("buildContext - agrega code y description cuando están presentes")
    void buildContext_codeYDescription() {
        Map<String, Object> beforeData = new LinkedHashMap<>();
        beforeData.put("code", "T-001");
        beforeData.put("description", "IVA 19%");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, beforeData);
        assertEquals(2, result.size());
        assertEquals("T-001", result.get("code"));
        assertEquals("IVA 19%", result.get("description"));
    }

    @Test
    @DisplayName("buildContext - solo code")
    void buildContext_soloCode() {
        Map<String, Object> beforeData = new LinkedHashMap<>();
        beforeData.put("code", "T-001");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, beforeData);
        assertEquals(1, result.size());
        assertEquals("T-001", result.get("code"));
    }

    @Test
    @DisplayName("buildContext - solo description")
    void buildContext_soloDescription() {
        Map<String, Object> beforeData = new LinkedHashMap<>();
        beforeData.put("description", "IVA 19%");
        Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, beforeData);
        assertEquals(1, result.size());
        assertEquals("IVA 19%", result.get("description"));
    }

    // metodo entityToMap
    @Test
    @DisplayName("entityToMap - tipo inválido retorna null")
    void entityToMap_tipoInvalido() {
        Map<String, Object> result = aspect.testEntityToMap(new Object());

        assertNull(result);
    }

    @Test
    @DisplayName("entityToMap - mapea correctamente Tax y remueve nulos")
    void entityToMap_tipoValido() {
        AccountCatalogueEntity salesTax = new AccountCatalogueEntity();
        salesTax.setId(10L);
        salesTax.setCode("S-10");

        AccountCatalogueEntity purchaseTax = new AccountCatalogueEntity();
        purchaseTax.setId(20L);
        purchaseTax.setCode("P-20");

        Tax tax = new Tax();
        tax.setId(1L);
        tax.setIdEnterprise("ENT-1");
        tax.setCode("T-001");
        tax.setDescription("IVA 19%");
        tax.setInterest(null);
        tax.setSalesTax(salesTax);
        tax.setPurchaseTax(purchaseTax);
        tax.setStatus(true);

        Map<String, Object> result = aspect.testEntityToMap(tax);

        assertEquals(1L, result.get("id"));
        assertEquals("ENT-1", result.get("entId"));
        assertEquals("T-001", result.get("code"));
        assertEquals("IVA 19%", result.get("description"));
        assertEquals(true, result.get("state"));
        assertEquals(10L, result.get("salesTaxId"));
        assertEquals("S-10", result.get("salesTaxCode"));
        assertEquals(20L, result.get("purchaseTaxId"));
        assertEquals("P-20", result.get("purchaseTaxCode"));

        assertFalse(result.containsKey("interest"));
    }

    // Helper
    private Auditable mockAuditable(OperationType operationType, String affectedTable,
            int idArgIndex, int enterpriseIdArgIndex) {
        Auditable auditable = mock(Auditable.class);
        lenient().when(auditable.operationType()).thenReturn(operationType);
        lenient().when(auditable.affectedTable()).thenReturn(affectedTable);
        lenient().when(auditable.idArgIndex()).thenReturn(idArgIndex);
        lenient().when(auditable.enterpriseIdArgIndex()).thenReturn(enterpriseIdArgIndex);
        return auditable;
    }

    private static class TestableAuditAspectTaxes extends AuditAspectTaxes {

        public TestableAuditAspectTaxes(AuditEventBuilder auditEventBuilder,
                AuditEventPublisher auditEventPublisher,
                ITaxSearchOutputPort taxSearchOutputPort) {
            super(auditEventBuilder, auditEventPublisher, taxSearchOutputPort);
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
