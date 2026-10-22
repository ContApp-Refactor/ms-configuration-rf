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

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.audit.AuditAspectAccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuditAspectCatalogueTest {

        @Mock
        private AuditEventBuilder auditEventBuilder;

        @Mock
        private AuditEventPublisher auditEventPublisher;

        @Mock
        private IAccountCatalogueSearchOutputPort accountCatalogueServiceSearch;

        @Mock
        private ProceedingJoinPoint joinPoint;

        private TestableAuditAspectAccountCatalogue aspect;

        @BeforeEach
        void setUp() {
                aspect = new TestableAuditAspectAccountCatalogue(
                                auditEventBuilder,
                                auditEventPublisher,
                                accountCatalogueServiceSearch);
        }

        // Metodo fetchcurrntstate
        @Test
        @DisplayName("fetchCurrentState - tabla no soportada retorna null")
        void fetchCurrentState_tablaInvalida() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "OTHER");
                Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
                assertNull(result);
        }

        @Test
        @DisplayName("fetchCurrentState - debe buscar usando enterpriseId String")
        void fetchCurrentState_conEnterpriseString() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "ACCOUNT_CATALOGUE");
                AccountCatalogue entity = new AccountCatalogue();
                entity.setId(1L);
                entity.setIdEnterprise("ENT-1");
                entity.setCode("100");
                when(accountCatalogueServiceSearch
                                .getAccountCatalogueByIdAndIdEnterprise(1L, "ENT-1"))
                                .thenReturn(entity);
                Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
                assertNotNull(result);
                assertEquals(1L, result.get("id"));
                verify(accountCatalogueServiceSearch)
                                .getAccountCatalogueByIdAndIdEnterprise(1L, "ENT-1");
        }

        @Test
        @DisplayName("fetchCurrentState - si servicio retorna null debe retornar null")
        void fetchCurrentState_servicioNull() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "ACCOUNT_CATALOGUE");
                when(accountCatalogueServiceSearch
                                .getAccountCatalogueByIdAndIdEnterprise(anyLong(), anyString()))
                                .thenReturn(null);
                Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, "ENT-1" });
                assertNull(result);
        }

        @Test
        @DisplayName("fetchCurrentState - debe obtener enterpriseId desde AccountCatalogue")
        void fetchCurrentState_conEnterpriseDesdeObjeto() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "ACCOUNT_CATALOGUE");
                AccountCatalogue request = new AccountCatalogue();
                request.setIdEnterprise("ENT-OBJ");
                AccountCatalogue entity = new AccountCatalogue();
                entity.setId(1L);
                entity.setIdEnterprise("ENT-OBJ");
                when(accountCatalogueServiceSearch
                                .getAccountCatalogueByIdAndIdEnterprise(1L, "ENT-OBJ"))
                                .thenReturn(entity);
                Map<String, Object> result = aspect.testFetchCurrentState(auditable, new Object[] { 1L, request });
                assertNotNull(result);
                verify(accountCatalogueServiceSearch)
                                .getAccountCatalogueByIdAndIdEnterprise(1L, "ENT-OBJ");
        }

        // Metodo resolveEnterpriseId
        @Test
        @DisplayName("resolveEnterpriseId - CREATE debe obtener enterprise del result")
        void resolveEnterpriseId_create() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "ACCOUNT_CATALOGUE");
                AccountCatalogue entity = new AccountCatalogue();
                entity.setIdEnterprise("ENT-1");
                String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, entity, null);
                assertEquals("ENT-1", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - CREATE con result inválido retorna UNKNOWN")
        void resolveEnterpriseId_createInvalido() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "ACCOUNT_CATALOGUE");
                String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, new Object(), null);
                assertEquals("UNKNOWN", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - UPDATE debe obtener entId desde beforeData")
        void resolveEnterpriseId_update() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "ACCOUNT_CATALOGUE");
                Map<String, Object> before = Map.of("entId", "ENT-1");
                String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
                assertEquals("ENT-1", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - UPDATE con beforeData null retorna UNKNOWN")
        void resolveEnterpriseId_updateBeforeNull() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "ACCOUNT_CATALOGUE");
                String result = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);
                assertEquals("UNKNOWN", result);
        }

        // Metodo resolveregisterid
        @Test
        @DisplayName("resolveRegisterId - CREATE debe obtener id desde result")
        void resolveRegisterId_create() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "ACCOUNT_CATALOGUE");
                AccountCatalogue entity = new AccountCatalogue();
                entity.setId(99L);
                String result = aspect.testResolveRegisterId(auditable, new Object[] {}, entity, null);
                assertEquals("99", result);
        }

        @Test
        @DisplayName("resolveRegisterId - CREATE inválido retorna UNKNOWN")
        void resolveRegisterId_createInvalido() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "ACCOUNT_CATALOGUE");
                String result = aspect.testResolveRegisterId(auditable, new Object[] {}, new Object(), null);
                assertEquals("UNKNOWN", result);
        }

        @Test
        @DisplayName("resolveRegisterId - UPDATE debe obtener id desde beforeData")
        void resolveRegisterId_update() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "ACCOUNT_CATALOGUE");
                Map<String, Object> before = Map.of("id", 15L);
                String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
                assertEquals("15", result);
        }

        @Test
        @DisplayName("resolveRegisterId - UPDATE con beforeData null retorna UNKNOWN")
        void resolveRegisterId_updateBeforeNull() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "ACCOUNT_CATALOGUE");
                String result = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);
                assertEquals("UNKNOWN", result);
        }

        // Metodo buildcontext
        @Test
        @DisplayName("buildContext - beforeData null retorna vacío")
        void buildContext_beforeNull() {
                Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, null);
                assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("buildContext - debe incluir code y description")
        void buildContext_completo() {
                Map<String, Object> before = Map.of("code", "100", "description", "Caja");
                Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
                assertEquals("100", result.get("code"));
                assertEquals("Caja", result.get("description"));
        }

        @Test
        @DisplayName("buildContext - debe incluir solo code")
        void buildContext_soloCode() {
                Map<String, Object> before = Map.of("code", "100");
                Map<String, Object> result = aspect.testBuildContext(
                                new Object[] {},
                                null,
                                before);
                assertEquals("100", result.get("code"));
                assertFalse(result.containsKey("description"));
        }

        @Test
        @DisplayName("buildContext - debe incluir solo description")
        void buildContext_soloDescription() {
                Map<String, Object> before = Map.of("description", "Caja");
                Map<String, Object> result = aspect.testBuildContext(new Object[] {}, null, before);
                assertEquals("Caja", result.get("description"));
                assertFalse(result.containsKey("code"));
        }

        // Entityto map
        @Test
        @DisplayName("entityToMap - objeto inválido retorna null")
        void entityToMap_objetoInvalido() {
                Map<String, Object> result = aspect.testEntityToMap(new Object());
                assertNull(result);
        }

        @Test
        @DisplayName("entityToMap - debe mapear correctamente")
        void entityToMap_ok() {
                AccountCatalogue entity = new AccountCatalogue();
                entity.setId(1L);
                entity.setIdEnterprise("ENT-1");
                entity.setCode("100");
                entity.setDescription("Caja");
                entity.setCrossing(true);
                entity.setCostCenter(false);
                entity.setStatus(true);
                Map<String, Object> result = aspect.testEntityToMap(entity);
                assertEquals(1L, result.get("id"));
                assertEquals("ENT-1", result.get("entId"));
                assertEquals("100", result.get("code"));
                assertEquals("Caja", result.get("description"));
                assertEquals(true, result.get("crossing"));
                assertEquals(false, result.get("costCenter"));
                assertEquals(true, result.get("state"));
        }

        @Test
        @DisplayName("entityToMap - debe eliminar campos null")
        void entityToMap_removeNulls() {
                AccountCatalogue entity = new AccountCatalogue();
                entity.setId(1L);
                Map<String, Object> result = aspect.testEntityToMap(entity);
                assertFalse(result.containsKey("description"));
                assertFalse(result.containsKey("parentId"));
                assertFalse(result.containsKey("nature"));
        }

        // Metodo audit
        @Test
        @DisplayName("audit - debe delegar a executeAudit")
        void audit_delegaExecuteAudit() throws Throwable {
                Auditable auditable = mockAuditable(OperationType.CREATE, "ACCOUNT_CATALOGUE");
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

        private static class TestableAuditAspectAccountCatalogue extends AuditAspectAccountCatalogue {
                public TestableAuditAspectAccountCatalogue(AuditEventBuilder auditEventBuilder,
                                AuditEventPublisher auditEventPublisher,
                                IAccountCatalogueSearchOutputPort accountCatalogueServiceSearch) {
                        super(auditEventBuilder, auditEventPublisher, accountCatalogueServiceSearch);
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
