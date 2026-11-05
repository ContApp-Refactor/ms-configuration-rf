package co.unicauca.edu.co.contables.inventory.products.unitAudit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.aspect.AuditAspect;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

        @Mock
        private AuditEventBuilder auditEventBuilder;

        @Mock
        private AuditEventPublisher auditEventPublisher;

        @Mock
        private IProductPersistencePort productPersistencePort;

        @Mock
        private ICategoryPersistencePort categoryPersistencePort;

        @Mock
        private IProductTypePersistencePort productTypePersistencePort;

        @Mock
        private IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;

        @Mock
        private ProceedingJoinPoint joinPoint;

        private TestableAuditAspect aspect;

        private Product product;

        @BeforeEach
        void setUp() {
                aspect = new TestableAuditAspect(
                                auditEventBuilder,
                                auditEventPublisher,
                                productPersistencePort,
                                categoryPersistencePort,
                                productTypePersistencePort,
                                unitOfMeasurePersistencePort);
                product = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .code("P-001")
                                .name("Producto")
                                .state(true)
                                .build();
        }

        // Metodo audit
        @Test
        @DisplayName("audit - CREATE debe publicar evento")
        void audit_create_ok() throws Throwable {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                when(joinPoint.getArgs()).thenReturn(new Object[] {});
                when(joinPoint.proceed()).thenReturn(product);
                OperationEventDto dto = new OperationEventDto();
                when(auditEventBuilder.build(
                                any(),
                                any(),
                                any(),
                                any(),
                                any()))
                                .thenReturn(dto);
                Object result = aspect.audit(joinPoint, auditable);
                assertNotNull(result);
                verify(joinPoint).proceed();
                verify(auditEventBuilder).build(
                                eq(auditable),
                                eq(OperationType.CREATE),
                                eq("ENT-1"),
                                eq("1"),
                                any());
                verify(auditEventPublisher).publish(dto);
        }

        @Test
        @DisplayName("audit - UPDATE debe generar changes y publicar")
        void audit_update_ok() throws Throwable {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                Product before = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .name("OLD")
                                .state(true)
                                .build();
                Product after = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .name("NEW")
                                .state(true)
                                .build();
                when(joinPoint.getArgs())
                                .thenReturn(new Object[] { 1L, "ENT-1" });
                when(productPersistencePort.findByIdAndEnterpriseId(1L, "ENT-1"))
                                .thenReturn(Optional.of(before))
                                .thenReturn(Optional.of(after));
                when(joinPoint.proceed()).thenReturn(after);
                when(auditEventBuilder.build(
                                any(),
                                any(),
                                any(),
                                any(),
                                any()))
                                .thenReturn(new OperationEventDto());
                aspect.audit(joinPoint, auditable);
                verify(auditEventPublisher).publish(any());
        }

        @Test
        @DisplayName("audit - si negocio falla no publica auditoria")
        void audit_negocioFalla() throws Throwable {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                when(joinPoint.getArgs()).thenReturn(new Object[] {});
                when(joinPoint.proceed())
                                .thenThrow(new RuntimeException("Error negocio"));
                assertThrows(RuntimeException.class,
                                () -> aspect.audit(joinPoint, auditable));
                verifyNoInteractions(auditEventPublisher);
                verifyNoInteractions(auditEventBuilder);
        }

        @Test
        @DisplayName("audit - error construyendo auditoria no rompe negocio")
        void audit_errorAuditoria_noRompe() throws Throwable {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                when(joinPoint.getArgs()).thenReturn(new Object[] {});
                when(joinPoint.proceed()).thenReturn(product);
                when(auditEventBuilder.build(
                                any(),
                                any(),
                                any(),
                                any(),
                                any()))
                                .thenThrow(new RuntimeException("Error auditoria"));
                Object result = aspect.audit(joinPoint, auditable);
                assertNotNull(result);
                verify(joinPoint).proceed();
                verifyNoInteractions(auditEventPublisher);
        }

        @Test
        @DisplayName("audit - UPDATE sin cambios no publica")
        void audit_update_sinCambios() throws Throwable {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                Product same = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .name("SAME")
                                .state(true)
                                .build();
                when(joinPoint.getArgs())
                                .thenReturn(new Object[] { 1L, "ENT-1" });
                when(productPersistencePort.findByIdAndEnterpriseId(1L, "ENT-1"))
                                .thenReturn(Optional.of(same))
                                .thenReturn(Optional.of(same));
                when(joinPoint.proceed()).thenReturn(same);
                Object result = aspect.audit(joinPoint, auditable);
                assertNotNull(result);
                verifyNoInteractions(auditEventPublisher);
        }

        // Metodo capturebeforedata
        @Test
        @DisplayName("captureBeforeData - UPDATE obtiene estado before")
        void captureBeforeData_update() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                when(productPersistencePort.findByIdAndEnterpriseId(1L, "ENT-1"))
                                .thenReturn(Optional.of(product));
                Map<String, Object> result = aspect.testCaptureBeforeData(
                                auditable,
                                new Object[] { 1L, "ENT-1" });
                assertNotNull(result);
                assertEquals("Producto", result.get("name"));
        }

        @Test
        @DisplayName("captureBeforeData - CREATE retorna null")
        void captureBeforeData_create() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                Map<String, Object> result = aspect.testCaptureBeforeData(
                                auditable,
                                new Object[] {});
                assertNull(result);
        }

        @Test
        @DisplayName("captureBeforeData - exception retorna null")
        void captureBeforeData_exception() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                when(productPersistencePort.findByIdAndEnterpriseId(1L, "ENT-1"))
                                .thenThrow(new RuntimeException("BD caída"));
                Map<String, Object> result = aspect.testCaptureBeforeData(
                                auditable,
                                new Object[] { 1L, "ENT-1" });
                assertNull(result);
        }

        // Metodo resolvefinaloperationtype
        @Test
        @DisplayName("resolveFinalOperationType - state true retorna INACTIVATE")
        void resolveFinalOperationType_inactivate() {
                Auditable auditable = mockAuditable(OperationType.INACTIVATE, "PRODUCT");
                Map<String, Object> before = Map.of("state", true);
                OperationType result = aspect.testResolveFinalOperationType(
                                auditable,
                                before);
                assertEquals(OperationType.INACTIVATE, result);
        }

        @Test
        @DisplayName("resolveFinalOperationType - state false retorna ACTIVATE")
        void resolveFinalOperationType_activate() {
                Auditable auditable = mockAuditable(OperationType.INACTIVATE, "PRODUCT");
                Map<String, Object> before = Map.of("state", false);
                OperationType result = aspect.testResolveFinalOperationType(
                                auditable,
                                before);
                assertEquals(OperationType.ACTIVATE, result);
        }

        // Metodo fetchcurrentstate
        @Test
        @DisplayName("fetchCurrentState - PRODUCT encontrado")
        void fetchCurrentState_product() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                when(productPersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.of(product));
                Map<String, Object> result = aspect.testFetchCurrentState(
                                auditable,
                                new Object[] { 1L, "ENT-1" });
                assertNotNull(result);
                assertEquals("Producto", result.get("name"));
                verify(productPersistencePort)
                                .findByIdAndEnterpriseId(1L, "ENT-1");
        }

        @Test
        @DisplayName("fetchCurrentState - PRODUCT no encontrado")
        void fetchCurrentState_productNoEncontrado() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                when(productPersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.empty());
                Map<String, Object> result = aspect.testFetchCurrentState(
                                auditable,
                                new Object[] { 1L, "ENT-1" });
                assertNull(result);
        }

        @Test
        @DisplayName("fetchCurrentState - CATEGORY encontrado")
        void fetchCurrentState_category() {

                Auditable auditable = mockAuditable(OperationType.UPDATE, "CATEGORY");

                Category category = Category.builder()
                                .id(1L)
                                .name("Categoria")
                                .state(true)
                                .build();

                when(categoryPersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.of(category));

                Map<String, Object> result = aspect.testFetchCurrentState(
                                auditable,
                                new Object[] { 1L, "ENT-1" });

                assertNotNull(result);

                assertEquals("Categoria", result.get("name"));
        }

        @Test
        @DisplayName("fetchCurrentState - PRODUCT_TYPE encontrado")
        void fetchCurrentState_productType() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT_TYPE");
                ProductType pt = ProductType.builder()
                                .id(1L)
                                .name("Tipo")
                                .state(true)
                                .build();
                when(productTypePersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.of(pt));
                Map<String, Object> result = aspect.testFetchCurrentState(
                                auditable,
                                new Object[] { 1L, "ENT-1" });
                assertNotNull(result);
                assertEquals("Tipo", result.get("name"));
        }

        @Test
        @DisplayName("fetchCurrentState - UNIT_OF_MEASURE encontrado")
        void fetchCurrentState_unitOfMeasure() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "UNIT_OF_MEASURE");
                UnitOfMeasure u = UnitOfMeasure.builder()
                                .id(1L)
                                .name("Unidad")
                                .abbreviation("UND")
                                .state(true)
                                .build();
                when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.of(u));
                Map<String, Object> result = aspect.testFetchCurrentState(
                                auditable,
                                new Object[] { 1L, "ENT-1" });
                assertNotNull(result);
                assertEquals("UND", result.get("abbreviation"));
        }

        @Test
        @DisplayName("fetchCurrentState - tabla desconocida retorna null")
        void fetchCurrentState_tablaDesconocida() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "OTRA");
                Map<String, Object> result = aspect.testFetchCurrentState(
                                auditable,
                                new Object[] { 1L, "ENT-1" });
                assertNull(result);
        }

        // Metodo buildataobject
        @Test
        @DisplayName("buildDataObject - CREATE PRODUCT")
        void buildDataObject_create_product() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.CREATE,
                                new Object[] {},
                                product,
                                null,
                                auditable);
                assertNotNull(result);
                assertTrue(result.containsKey("entity"));
                Map<?, ?> entity = (Map<?, ?>) result.get("entity");
                assertEquals("Producto", entity.get("name"));
        }

        @Test
        @DisplayName("buildDataObject - CREATE CATEGORY")
        void buildDataObject_create_category() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "CATEGORY");
                Category category = Category.builder()
                                .id(1L)
                                .name("Categoria")
                                .state(true)
                                .build();
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.CREATE,
                                new Object[] {},
                                category,
                                null,
                                auditable);
                Map<?, ?> entity = (Map<?, ?>) result.get("entity");
                assertEquals("Categoria", entity.get("name"));
        }

        @Test
        @DisplayName("buildDataObject - CREATE PRODUCT_TYPE")
        void buildDataObject_create_productType() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT_TYPE");
                ProductType pt = ProductType.builder()
                                .id(1L)
                                .name("Tipo")
                                .state(true)
                                .build();
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.CREATE,
                                new Object[] {},
                                pt,
                                null,
                                auditable);
                assertTrue(result.containsKey("entity"));
                Map<?, ?> entity = (Map<?, ?>) result.get("entity");
                assertEquals("Tipo", entity.get("name"));
        }

        @Test
        @DisplayName("buildDataObject - CREATE UNIT_OF_MEASURE")
        void buildDataObject_create_unitOfMeasure() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "UNIT_OF_MEASURE");
                UnitOfMeasure uom = UnitOfMeasure.builder()
                                .id(1L)
                                .name("Kilogramo")
                                .abbreviation("KG")
                                .state(true)
                                .build();
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.CREATE,
                                new Object[] {},
                                uom,
                                null,
                                auditable);
                assertTrue(result.containsKey("entity"));
                Map<?, ?> entity = (Map<?, ?>) result.get("entity");
                assertEquals("KG", entity.get("abbreviation"));
        }

        @Test
        @DisplayName("buildDataObject - UPDATE con cambios")
        void buildDataObject_update_conCambios() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                Product before = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .code("P1")
                                .name("OLD")
                                .state(true)
                                .build();
                Product after = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .code("P1")
                                .name("NEW")
                                .state(true)
                                .build();
                when(productPersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.of(after));
                Map<String, Object> beforeMap = aspect.testProductToMap(before);
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.UPDATE,
                                new Object[] { 1L, "ENT-1" },
                                after,
                                beforeMap,
                                auditable);
                assertTrue(result.containsKey("changes"));
                Map<?, ?> changes = (Map<?, ?>) result.get("changes");
                assertTrue(changes.containsKey("name"));
        }

        @Test
        @DisplayName("buildDataObject - UPDATE sin cambios")
        void buildDataObject_update_sinCambios() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                when(productPersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.of(product));
                Map<String, Object> before = aspect.testProductToMap(product);
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.UPDATE,
                                new Object[] { 1L, "ENT-1" },
                                product,
                                before,
                                auditable);
                Map<?, ?> changes = (Map<?, ?>) result.get("changes");
                assertTrue(changes.isEmpty());
        }

        @Test
        @DisplayName("buildDataObject - UPDATE incluye context")
        void buildDataObject_update_context() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                Product after = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .code("P1")
                                .name("NEW")
                                .state(true)
                                .build();
                when(productPersistencePort.findByIdAndEnterpriseId(
                                1L,
                                "ENT-1"))
                                .thenReturn(Optional.of(after));
                Map<String, Object> before = new LinkedHashMap<>();
                before.put("code", "P1");
                before.put("name", "OLD");
                before.put("state", true);
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.UPDATE,
                                new Object[] { 1L, "ENT-1" },
                                after,
                                before,
                                auditable);
                assertTrue(result.containsKey("context"));
                Map<?, ?> context = (Map<?, ?>) result.get("context");
                assertEquals("P1", context.get("code"));
        }

        @Test
        @DisplayName("buildDataObject - INACTIVATE genera diff state")
        void buildDataObject_inactivate() {
                Auditable auditable = mockAuditable(OperationType.INACTIVATE, "PRODUCT");
                Map<String, Object> before = new LinkedHashMap<>();
                before.put("state", true);
                before.put("code", "P1");
                before.put("name", "Producto");
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.INACTIVATE,
                                new Object[] { 1L, "ENT-1" },
                                null,
                                before,
                                auditable);
                Map<?, ?> changes = (Map<?, ?>) result.get("changes");
                assertTrue(changes.containsKey("state"));
        }

        @Test
        @DisplayName("buildDataObject - INACTIVATE sin beforeData retorna vacio")
        void buildDataObject_inactivate_sinBefore() {
                Auditable auditable = mockAuditable(OperationType.INACTIVATE, "PRODUCT");
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.INACTIVATE,
                                new Object[] { 1L, "ENT-1" },
                                null,
                                null,
                                auditable);
                assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("buildDataObject - DELETE con beforeData")
        void buildDataObject_delete_conBefore() {
                Auditable auditable = mockAuditable(OperationType.DELETE, "PRODUCT");
                Map<String, Object> before = new LinkedHashMap<>();
                before.put("id", 1L);
                before.put("name", "Producto");
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.DELETE,
                                new Object[] { 1L, "ENT-1" },
                                null,
                                before,
                                auditable);
                Map<?, ?> entity = (Map<?, ?>) result.get("entity");
                assertEquals("Producto", entity.get("name"));
        }

        @Test
        @DisplayName("buildDataObject - DELETE sin beforeData usa id args")
        void buildDataObject_delete_sinBefore() {
                Auditable auditable = mockAuditable(OperationType.DELETE, "PRODUCT");
                Map<String, Object> result = aspect.testBuildDataObject(
                                OperationType.DELETE,
                                new Object[] { 1L, "ENT-1" },
                                null,
                                null,
                                auditable);
                Map<?, ?> entity = (Map<?, ?>) result.get("entity");
                assertEquals(1L, entity.get("id"));
        }

        // Metodo buildContext
        @Test
        @DisplayName("buildContext - Product debe incluir code y name")
        void buildContext_product_ok() {
                Map<String, Object> data = Map.of(
                                "code", "P-001",
                                "name", "Producto",
                                "description", "Desc");
                Map<String, Object> result = aspect.testBuildContext(Product.class, data);
                assertEquals(2, result.size());
                assertEquals("P-001", result.get("code"));
                assertEquals("Producto", result.get("name"));
        }

        @Test
        @DisplayName("buildContext - UnitOfMeasure debe incluir name y abbreviation")
        void buildContext_unitOfMeasure_ok() {
                Map<String, Object> data = Map.of(
                                "name", "Kilogramo",
                                "abbreviation", "KG",
                                "description", "Unidad");
                Map<String, Object> result = aspect.testBuildContext(UnitOfMeasure.class, data);
                assertEquals(2, result.size());
                assertEquals("Kilogramo", result.get("name"));
                assertEquals("KG", result.get("abbreviation"));
        }

        @Test
        @DisplayName("buildContext - data null retorna vacío")
        void buildContext_dataNull() {
                Map<String, Object> result = aspect.testBuildContext(Product.class, null);
                assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("buildContext - clase desconocida retorna vacío")
        void buildContext_unknownClass() {
                Map<String, Object> data = Map.of(
                                "name", "Test");
                Map<String, Object> result = aspect.testBuildContext(String.class, data);
                assertTrue(result.isEmpty());
        }

        // Metodo resolveEnterpriseId
        @Test
        @DisplayName("resolveEnterpriseId - CREATE Product")
        void resolveEnterpriseId_create_product() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                String result = aspect.testResolveEnterpriseId(
                                auditable,
                                new Object[] {},
                                product);
                assertEquals("ENT-1", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - CREATE Category")
        void resolveEnterpriseId_create_category() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "CATEGORY");
                Category category = Category.builder()
                                .id(1L)
                                .enterpriseId("ENT-CAT")
                                .name("Categoria")
                                .build();
                String result = aspect.testResolveEnterpriseId(
                                auditable,
                                new Object[] {},
                                category);
                assertEquals("ENT-CAT", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - CREATE ProductType")
        void resolveEnterpriseId_create_productType() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT_TYPE");
                ProductType pt = ProductType.builder()
                                .id(1L)
                                .enterpriseId("ENT-PT")
                                .name("Tipo")
                                .build();
                String result = aspect.testResolveEnterpriseId(
                                auditable,
                                new Object[] {},
                                pt);
                assertEquals("ENT-PT", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - CREATE UnitOfMeasure")
        void resolveEnterpriseId_create_unitOfMeasure() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "UNIT_OF_MEASURE");
                UnitOfMeasure uom = UnitOfMeasure.builder()
                                .id(1L)
                                .enterpriseId("ENT-UOM")
                                .name("KG")
                                .build();
                String result = aspect.testResolveEnterpriseId(
                                auditable,
                                new Object[] {},
                                uom);
                assertEquals("ENT-UOM", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - CREATE desconocido retorna UNKNOWN")
        void resolveEnterpriseId_create_unknown() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                String result = aspect.testResolveEnterpriseId(
                                auditable,
                                new Object[] {},
                                new Object());
                assertEquals("UNKNOWN", result);
        }

        @Test
        @DisplayName("resolveEnterpriseId - default usa args")
        void resolveEnterpriseId_default_usaArgs() {
                Auditable auditable = mockAuditable(OperationType.UPDATE, "PRODUCT");
                String result = aspect.testResolveEnterpriseId(
                                auditable,
                                new Object[] { 1L, "ENT-ARGS" },
                                null);
                assertEquals("ENT-ARGS", result);
        }

        // Metodo resolveRegisterId
        @Test
        @DisplayName("resolveRegisterId - CREATE Product")
        void resolveRegisterId_create_product() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                String result = aspect.testResolveRegisterId(
                                auditable,
                                new Object[] {},
                                product);
                assertEquals("1", result);
        }

        @Test
        @DisplayName("resolveRegisterId - CREATE Category")
        void resolveRegisterId_create_category() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "CATEGORY");
                Category category = Category.builder()
                                .id(10L)
                                .enterpriseId("ENT-CAT")
                                .name("Categoria")
                                .build();
                String result = aspect.testResolveRegisterId(
                                auditable,
                                new Object[] {},
                                category);
                assertEquals("10", result);
        }

        @Test
        @DisplayName("resolveRegisterId - CREATE ProductType")
        void resolveRegisterId_create_productType() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT_TYPE");
                ProductType pt = ProductType.builder()
                                .id(20L)
                                .enterpriseId("ENT-PT")
                                .name("Tipo")
                                .build();
                String result = aspect.testResolveRegisterId(
                                auditable,
                                new Object[] {},
                                pt);
                assertEquals("20", result);
        }

        @Test
        @DisplayName("resolveRegisterId - CREATE desconocido retorna UNKNOWN")
        void resolveRegisterId_create_unknown() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                String result = aspect.testResolveRegisterId(
                                auditable,
                                new Object[] {},
                                new Object());
                assertEquals("UNKNOWN", result);
        }

        // metodos mapper
        @Test
        @DisplayName("productToMap - mapea correctamente")
        void productToMap_ok() {
                Product p = Product.builder()
                                .id(1L)
                                .code("P-001")
                                .name("Producto")
                                .description("Desc")
                                .quantity(10)
                                .cost(10L)
                                .unitOfMeasureId(2L)
                                .categoryId(3L)
                                .productTypeId(4L)
                                .reference("REF")
                                .presentation("Caja")
                                .state(true)
                                .build();
                Map<String, Object> result = aspect.testProductToMap(p);
                assertAll(
                                () -> assertEquals(1L, result.get("id")),
                                () -> assertEquals("P-001", result.get("code")),
                                () -> assertEquals("Producto", result.get("name")),
                                () -> assertEquals(true, result.get("state")));
        }

        @Test
        @DisplayName("productToMap - elimina campos null")
        void productToMap_removeNulls() {
                Product p = Product.builder()
                                .id(1L)
                                .name("Producto")
                                .state(true)
                                .build();
                Map<String, Object> result = aspect.testProductToMap(p);
                assertFalse(result.containsKey("description"));
                assertFalse(result.containsKey("reference"));
                assertFalse(result.containsKey("presentation"));
        }

        @Test
        @DisplayName("categoryToMap - mapea correctamente")
        void categoryToMap_ok() {
                Category c = Category.builder()
                                .id(1L)
                                .name("Cat")
                                .description("Desc")
                                .inventoryId(10L)
                                .state(true)
                                .build();
                Map<String, Object> result = aspect.testCategoryToMap(c);
                assertAll(
                                () -> assertEquals(1L, result.get("id")),
                                () -> assertEquals("Cat", result.get("name")),
                                () -> assertEquals(true, result.get("state")));
        }

        @Test
        @DisplayName("unitOfMeasureToMap - mapea correctamente")
        void unitOfMeasureToMap_ok() {
                UnitOfMeasure u = UnitOfMeasure.builder()
                                .id(1L)
                                .name("Kilogramo")
                                .abbreviation("KG")
                                .description("Peso")
                                .state(true)
                                .build();
                Map<String, Object> result = aspect.testUnitOfMeasureToMap(u);
                assertAll(
                                () -> assertEquals(1L, result.get("id")),
                                () -> assertEquals("Kilogramo", result.get("name")),
                                () -> assertEquals("KG", result.get("abbreviation")),
                                () -> assertEquals(true, result.get("state")));
        }

        @Test
        @DisplayName("resolveFinalOperationType - CREATE retorna CREATE")
        void resolveFinalOperationType_create() {
                Auditable auditable = mockAuditable(OperationType.CREATE, "PRODUCT");
                OperationType result = aspect.testResolveFinalOperationType(
                                auditable,
                                null);
                assertEquals(OperationType.CREATE, result);
        }

        // Helper
        private Auditable mockAuditable(OperationType type, String table) {
                Auditable a = mock(Auditable.class);
                lenient().when(a.operationType()).thenReturn(type);
                lenient().when(a.affectedTable()).thenReturn(table);
                lenient().when(a.idArgIndex()).thenReturn(0);
                lenient().when(a.enterpriseIdArgIndex()).thenReturn(1);

                return a;
        }

        private static class TestableAuditAspect extends AuditAspect {
                public TestableAuditAspect(
                                AuditEventBuilder auditEventBuilder,
                                AuditEventPublisher auditEventPublisher,
                                IProductPersistencePort productPersistencePort,
                                ICategoryPersistencePort categoryPersistencePort,
                                IProductTypePersistencePort productTypePersistencePort,
                                IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort) {
                        super(
                                        auditEventBuilder,
                                        auditEventPublisher,
                                        productPersistencePort,
                                        categoryPersistencePort,
                                        productTypePersistencePort,
                                        unitOfMeasurePersistencePort);
                }

                public Map<String, Object> testCaptureBeforeData(Auditable auditable, Object[] args) {
                        return super.captureBeforeData(auditable, args);
                }

                public OperationType testResolveFinalOperationType(Auditable auditable,
                                Map<String, Object> beforeData) {
                        return super.resolveFinalOperationType(auditable, beforeData);
                }

                public Map<String, Object> testFetchCurrentState(Auditable auditable, Object[] args) {
                        return super.fetchCurrentState(auditable, args);
                }

                public Map<String, Object> testBuildDataObject(OperationType operationType, Object[] args,
                                Object result, Map<String, Object> beforeData, Auditable auditable) {
                        return super.buildDataObject(operationType, args, result, beforeData, auditable);
                }

                public Map<String, Object> testProductToMap(Product p) {
                        return super.productToMap(p);
                }

                public Map<String, Object> testBuildContext(Class<?> entityClass, Map<String, Object> data) {
                        return super.buildContext(entityClass, data);
                }

                public String testResolveEnterpriseId(Auditable auditable, Object[] args, Object result) {
                        return super.resolveEnterpriseId(auditable, args, result);
                }

                public String testResolveRegisterId(Auditable auditable, Object[] args, Object result) {
                        return super.resolveRegisterId(auditable, args, result);
                }

                public Map<String, Object> testCategoryToMap(Category c) {
                        return super.categoryToMap(c);
                }

                public Map<String, Object> testUnitOfMeasureToMap(UnitOfMeasure u) {
                        return super.unitOfMeasureToMap(u);
                }
        }
}
