package co.unicauca.edu.co.contables.configuration.unitAudit.auditCommon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;

@ExtendWith(MockitoExtension.class)
class BaseAuditAspectTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;
    @Mock
    private AuditEventPublisher auditEventPublisher;
    @Mock
    private ProceedingJoinPoint joinPoint;

    // Subclase concreta mínima para poder instanciar la clase abstracta
    private TestableBaseAuditAspect aspect;

    // Estado interno para controlar fetchCurrentState en cada test
    private Map<String, Object> stubbedCurrentState;
    private boolean throwOnFetchCurrentState;
    private Map<String, Object> stubbedEntityMap;

    @BeforeEach
    void setUp() {
        stubbedCurrentState = null;
        throwOnFetchCurrentState = false;
        stubbedEntityMap = null;
        aspect = new TestableBaseAuditAspect(
                auditEventBuilder,
                auditEventPublisher);
    }

    // Metodo buildDiff
    @Test
    @DisplayName("buildDiff - Debe detectar campos cambiados correctamente")
    void buildDiff_camposCambiados() {
        Map<String, Object> before = Map.of("name", "Facturas", "state", true);
        Map<String, Object> after = Map.of("name", "Facturas v2", "state", true);

        Map<String, Object> diff = aspect.testBuildDiff(before, after);

        assertEquals(1, diff.size());
        assertTrue(diff.containsKey("name"));
        Map<?, ?> nameChange = (Map<?, ?>) diff.get("name");
        assertEquals("Facturas", nameChange.get("before"));
        assertEquals("Facturas v2", nameChange.get("after"));
    }

    @Test
    @DisplayName("buildDiff - Debe retornar mapa vacío cuando no hay cambios")
    void buildDiff_sinCambios() {
        Map<String, Object> before = Map.of("name", "Facturas");
        Map<String, Object> after = Map.of("name", "Facturas");

        Map<String, Object> diff = aspect.testBuildDiff(before, after);

        assertTrue(diff.isEmpty());
    }

    @Test
    @DisplayName("buildDiff - Debe retornar mapa vacío cuando before es null")
    void buildDiff_beforeNull() {
        Map<String, Object> diff = aspect.testBuildDiff(null, Map.of("name", "Facturas"));
        assertTrue(diff.isEmpty());
    }

    @Test
    @DisplayName("buildDiff - Debe retornar mapa vacío cuando after es null")
    void buildDiff_afterNull() {
        Map<String, Object> diff = aspect.testBuildDiff(Map.of("name", "Facturas"), null);
        assertTrue(diff.isEmpty());
    }

    // Metodo captureBeforeData

    @Test
    @DisplayName("captureBeforeData - Debe retornar null para CREATE")
    void captureBeforeData_create_retornaNull() {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        assertNull(aspect.testCaptureBeforeData(auditable, new Object[] {}));
    }

    @Test
    @DisplayName("captureBeforeData - Debe capturar estado para UPDATE")
    void captureBeforeData_update_captura() {
        Auditable auditable = mockAuditable(OperationType.UPDATE);
        stubbedCurrentState = Map.of("name", "Facturas", "id", 1L);

        Map<String, Object> result = aspect.testCaptureBeforeData(auditable, new Object[] { 1L });

        assertNotNull(result);
        assertEquals("Facturas", result.get("name"));
    }

    @Test
    @DisplayName("captureBeforeData - Debe retornar null si fetchCurrentState lanza excepción")
    void captureBeforeData_excepcion_retornaNull() {
        Auditable auditable = mockAuditable(OperationType.DELETE);
        throwOnFetchCurrentState = true;
        assertNull(
                aspect.testCaptureBeforeData(
                        auditable,
                        new Object[] { 1L }));
    }

    // MEtodo resolveOperationType

    @Test
    @DisplayName("resolveFinalOperationType - CREATE retorna CREATE sin analizar resultado")
    void resolveFinalOperationType_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        assertEquals(OperationType.CREATE, aspect.testResolveFinalOperationType(auditable, null, null));
    }

    @Test
    @DisplayName("resolveFinalOperationType - INACTIVATE con state=true en result → ACTIVATE")
    void resolveFinalOperationType_inactivate_resultStateTrue_retornaActivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> result = Map.of("state", true);

        assertEquals(OperationType.ACTIVATE, aspect.testResolveFinalOperationType(auditable, result, null));
    }

    @Test
    @DisplayName("resolveFinalOperationType - INACTIVATE con state=false en result → INACTIVATE")
    void resolveFinalOperationType_inactivate_resultStateFalse_retornaInactivate() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> result = Map.of("state", false);

        assertEquals(OperationType.INACTIVATE, aspect.testResolveFinalOperationType(auditable, result, null));
    }

    @Test
    @DisplayName("resolveFinalOperationType - INACTIVATE sin state en result, beforeData activo → INACTIVATE")
    void resolveFinalOperationType_inactivate_sinResultState_beforeActivo() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> before = Map.of("state", true);

        assertEquals(OperationType.INACTIVATE, aspect.testResolveFinalOperationType(auditable, null, before));
    }

    @Test
    @DisplayName("resolveFinalOperationType - INACTIVATE sin state en result, beforeData inactivo → ACTIVATE")
    void resolveFinalOperationType_inactivate_sinResultState_beforeInactivo() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> before = Map.of("state", false);

        assertEquals(OperationType.ACTIVATE, aspect.testResolveFinalOperationType(auditable, null, before));
    }

    @Test
    @DisplayName("resolveFinalOperationType - UPDATE retorna UPDATE")
    void resolveFinalOperationType_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE);
        OperationType result = aspect.testResolveFinalOperationType(
                auditable,
                null,
                null);
        assertEquals(OperationType.UPDATE, result);
    }

    @Test
    @DisplayName("resolveFinalOperationType - DELETE retorna DELETE")
    void resolveFinalOperationType_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE);
        OperationType result = aspect.testResolveFinalOperationType(
                auditable,
                null,
                null);
        assertEquals(OperationType.DELETE, result);
    }

    // Metodo buildDataobject
    @Test
    @DisplayName("buildDataObject - CREATE con result null debe retornar mapa vacío")
    void buildDataObject_create_resultNull() {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        Map<String, Object> data = aspect.testBuildDataObject(
                OperationType.CREATE,
                new Object[] {},
                null,
                null,
                auditable);
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("buildDataObject - CREATE con result no null debe incluir entity")
    void buildDataObject_create_conResult() {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", 1L);
        result.put("name", "Facturas");

        Map<String, Object> data = aspect.testBuildDataObject(OperationType.CREATE, new Object[] {}, result, null,
                auditable);

        assertTrue(data.containsKey("entity"));
    }

    @Test
    @DisplayName("buildDataObject - UPDATE con cambios reales debe incluir changes no vacío")
    void buildDataObject_update_conCambios() {
        Auditable auditable = mockAuditable(OperationType.UPDATE);
        Map<String, Object> before = Map.of("name", "Facturas");
        stubbedCurrentState = Map.of("name", "Facturas v2"); // afterData viene de fetchCurrentState

        Map<String, Object> data = aspect.testBuildDataObject(OperationType.UPDATE, new Object[] { 1L }, null, before,
                auditable);

        assertTrue(data.containsKey("changes"));
        Map<?, ?> changes = (Map<?, ?>) data.get("changes");
        assertFalse(changes.isEmpty());
    }

    @Test
    @DisplayName("buildDataObject - UPDATE sin cambios debe retornar changes vacío")
    void buildDataObject_update_sinCambios() {
        Auditable auditable = mockAuditable(OperationType.UPDATE);
        Map<String, Object> before = Map.of("name", "Facturas");
        stubbedCurrentState = Map.of("name", "Facturas"); // sin cambios

        Map<String, Object> data = aspect.testBuildDataObject(OperationType.UPDATE, new Object[] { 1L }, null, before,
                auditable);

        Map<?, ?> changes = (Map<?, ?>) data.get("changes");
        assertTrue(changes.isEmpty());
    }

    @Test
    @DisplayName("buildDataObject - DELETE con beforeData debe incluirlo como entity")
    void buildDataObject_delete_conBeforeData() {
        Auditable auditable = mockAuditable(OperationType.DELETE);
        Map<String, Object> before = Map.of("id", 5L, "name", "Facturas");

        Map<String, Object> data = aspect.testBuildDataObject(OperationType.DELETE, new Object[] { 5L }, null, before,
                auditable);

        assertEquals(before, data.get("entity"));
    }

    @Test
    @DisplayName("buildDataObject - DELETE sin beforeData debe usar id de args")
    void buildDataObject_delete_sinBeforeData() {

        Auditable auditable = mockAuditable(OperationType.DELETE);

        Map<String, Object> data = aspect.testBuildDataObject(
                OperationType.DELETE,
                new Object[] { 99L },
                null,
                null,
                auditable);

        assertTrue(data.containsKey("entity"));

        Map<?, ?> entity = (Map<?, ?>) data.get("entity");

        assertEquals(99L, entity.get("id"));
    }

    @Test
    @DisplayName("buildDataObject - ACTIVATE/INACTIVATE con mismo estado debe retornar mapa vacío")
    void buildDataObject_activate_mismoEstado_retornaVacio() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> before = Map.of("state", true);
        // result con state=true también → no hubo cambio real
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("state", true);

        Map<String, Object> data = aspect.testBuildDataObject(OperationType.INACTIVATE, new Object[] { 1L }, result,
                before,
                auditable);

        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("buildDataObject - ACTIVATE/INACTIVATE sin beforeData debe retornar vacío")
    void buildDataObject_inactivate_sinBeforeData_retornaVacio() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> result = Map.of("state", false);
        Map<String, Object> data = aspect.testBuildDataObject(
                OperationType.INACTIVATE,
                new Object[] { 1L },
                result,
                null,
                auditable);
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("buildDataObject - ACTIVATE/INACTIVATE con cambio de estado debe incluir changes")
    void buildDataObject_inactivate_conCambio_real() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> before = Map.of("state", true);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("state", false);
        Map<String, Object> data = aspect.testBuildDataObject(
                OperationType.INACTIVATE,
                new Object[] { 1L },
                result,
                before,
                auditable);
        assertFalse(data.isEmpty());
        Map<?, ?> changes = (Map<?, ?>) data.get("changes");
        assertNotNull(changes);
        assertTrue(changes.containsKey("state"));
    }

    @Test
    @DisplayName("buildDataObject - ACTIVATE/INACTIVATE cuando entityToMap retorna null debe invertir estado")
    void buildDataObject_inactivate_entityToMapNull_invierteEstado() {
        Auditable auditable = mockAuditable(OperationType.INACTIVATE);
        Map<String, Object> before = Map.of("state", true);
        // fuerza entityToMap(result) => null
        stubbedEntityMap = null;
        Object result = new Object();
        Map<String, Object> data = aspect.testBuildDataObject(
                OperationType.INACTIVATE,
                new Object[] { 1L },
                result,
                before,
                auditable);
        assertFalse(data.isEmpty());
        Map<?, ?> changes = (Map<?, ?>) data.get("changes");
        assertNotNull(changes);
        assertTrue(changes.containsKey("state"));
        Map<?, ?> stateChange = (Map<?, ?>) changes.get("state");
        assertEquals(true, stateChange.get("before"));
        assertEquals(false, stateChange.get("after"));
    }

    // Metodo executeAudit
    @Test
    @DisplayName("executeAudit - Debe retornar resultado del negocio aunque falle la auditoría")
    void executeAudit_errorEnAuditoria_noRompeNegocio() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        Object businessResult = new Object();
        when(joinPoint.getArgs()).thenReturn(new Object[] {});
        when(joinPoint.proceed()).thenReturn(businessResult);
        when(auditEventBuilder.build(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Error inesperado en auditoría"));
        stubbedEntityMap = Map.of("id", 1L);
        Object returned = aspect.executeAudit(joinPoint, auditable);
        assertSame(businessResult, returned);
    }

    @Test
    @DisplayName("executeAudit - Debe propagar excepción del negocio")
    void executeAudit_excepcionNegocio_sepropaga() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        when(joinPoint.getArgs()).thenReturn(new Object[] {});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Error de negocio"));

        assertThrows(RuntimeException.class, () -> aspect.executeAudit(joinPoint, auditable));
        verifyNoInteractions(auditEventBuilder, auditEventPublisher);
    }

    @Test
    @DisplayName("executeAudit - No debe publicar si dataObject está vacío")
    void executeAudit_dataObjectVacio_noPublica() throws Throwable {
        // CREATE con result null → entity null → dataObject vacío
        Auditable auditable = mockAuditable(OperationType.CREATE);
        when(joinPoint.getArgs()).thenReturn(new Object[] {});
        when(joinPoint.proceed()).thenReturn(null);

        aspect.executeAudit(joinPoint, auditable);

        verifyNoInteractions(auditEventBuilder, auditEventPublisher);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("executeAudit - Happy path debe construir y publicar evento")
    void executeAudit_happyPath_publicaEvento() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        Object businessResult = new LinkedHashMap<>();
        ((Map<String, Object>) businessResult).put("id", 1L);
        OperationEventDto dto = OperationEventDto.builder()
                .operationType("CREATE")
                .build();
        when(joinPoint.getArgs()).thenReturn(new Object[] {});
        when(joinPoint.proceed()).thenReturn(businessResult);
        when(auditEventBuilder.build(
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(dto);
        Object result = aspect.executeAudit(joinPoint, auditable);
        assertSame(businessResult, result);
        verify(auditEventBuilder).build(
                any(),
                eq(OperationType.CREATE),
                eq("ENT-001"),
                eq("1"),
                any());
        verify(auditEventPublisher).publish(dto);
    }

    @Test
    @DisplayName("executeAudit - No debe publicar si changes está vacío")
    void executeAudit_changesVacio_noPublica() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.UPDATE);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 1L });
        Map<String, Object> businessResult = Map.of(
                "name", "Facturas");
        when(joinPoint.proceed()).thenReturn(businessResult);
        stubbedCurrentState = Map.of(
                "name", "Facturas");
        aspect.executeAudit(joinPoint, auditable);
        verifyNoInteractions(auditEventBuilder, auditEventPublisher);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("executeAudit - Debe retornar resultado aunque falle publish")
    void executeAudit_publishFalla_noRompeFlujo() throws Throwable {
        Auditable auditable = mockAuditable(OperationType.CREATE);
        Object businessResult = new LinkedHashMap<>();
        ((Map<String, Object>) businessResult).put("id", 1L);
        OperationEventDto dto = OperationEventDto.builder()
                .operationType("CREATE")
                .build();
        when(joinPoint.getArgs()).thenReturn(new Object[] {});
        when(joinPoint.proceed()).thenReturn(businessResult);
        when(auditEventBuilder.build(
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(dto);
        doThrow(new RuntimeException("Kafka down"))
                .when(auditEventPublisher)
                .publish(any());
        Object result = aspect.executeAudit(joinPoint, auditable);
        assertSame(businessResult, result);
        verify(auditEventPublisher).publish(dto);
    }

    // Helper
    private Auditable mockAuditable(OperationType type) {
        Auditable a = mock(Auditable.class);

        lenient().when(a.operationType()).thenReturn(type);
        lenient().when(a.affectedTable()).thenReturn("DOCUMENT_CLASS");
        lenient().when(a.moduleName()).thenReturn("CLASSES_OF_DOCUMENTS");
        lenient().when(a.idArgIndex()).thenReturn(0);
        lenient().when(a.enterpriseIdArgIndex()).thenReturn(1);

        return a;
    }

    private class TestableBaseAuditAspect extends BaseAuditAspect {
        protected TestableBaseAuditAspect(
                AuditEventBuilder auditEventBuilder,
                AuditEventPublisher auditEventPublisher) {

            super(auditEventBuilder, auditEventPublisher);
        }

        @Override
        protected Map<String, Object> fetchCurrentState(
                Auditable auditable,
                Object[] args) {

            if (throwOnFetchCurrentState) {
                throw new RuntimeException("BD caída");
            }

            return stubbedCurrentState;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Map<String, Object> entityToMap(Object object) {
            if (stubbedEntityMap != null) {
                return stubbedEntityMap;
            }

            if (object instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }

            return null;
        }

        @Override
        protected String resolveEnterpriseId(
                Auditable auditable,
                Object[] args,
                Object result,
                Map<String, Object> beforeData) {
            return "ENT-001";
        }

        @Override
        protected String resolveRegisterId(
                Auditable auditable,
                Object[] args,
                Object result,
                Map<String, Object> beforeData) {

            return "1";
        }

        public Map<String, Object> testBuildDiff(
                Map<String, Object> before,
                Map<String, Object> after) {
            return buildDiff(before, after);
        }

        public Map<String, Object> testBuildDataObject(
                OperationType operationType,
                Object[] args,
                Object result,
                Map<String, Object> beforeData,
                Auditable auditable) {
            return buildDataObject(
                    operationType,
                    args,
                    result,
                    beforeData,
                    auditable);
        }

        public Map<String, Object> testCaptureBeforeData(
                Auditable auditable,
                Object[] args) {
            return captureBeforeData(auditable, args);
        }

        public OperationType testResolveFinalOperationType(
                Auditable auditable,
                Object result,
                Map<String, Object> beforeData) {
            return resolveFinalOperationType(
                    auditable,
                    result,
                    beforeData);
        }
    }

}
