package co.unicauca.edu.co.contables.configuration.integrationAudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.audit.AuditAspectClassesDocuments;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services.IDocumentClassService;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services.TestClassesDocumentsAuditService;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.configuration.commons.security.IJwtUtils;

@SpringBootTest(classes = {
        AuditAspectClassesDocuments.class,
        AuditEventBuilder.class,
        TestClassesDocumentsAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectClassesDocumentsTest {

    @Autowired
    private TestClassesDocumentsAuditService testAuditService;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private IDocumentClassService documentClassService;

    @Test
    void should_intercept_and_publish_audit_event() {

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testAuditService.create();

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("CREATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
    }

    @Test
    void should_intercept_update_and_publish_audit_event() {

        DocumentClass before = DocumentClass.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .name("OLD")
                .status(true)
                .build();

        DocumentClass after = DocumentClass.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .name("NEW")
                .status(true)
                .build();

        when(documentClassService.findById(1L, "ENT-1"))
                .thenReturn(before)
                .thenReturn(after);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        DocumentClassUpdateReq req = new DocumentClassUpdateReq();
        req.setId(1L);
        req.setIdEnterprise("ENT-1");
        req.setName("NEW");

        testAuditService.update(req);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("UPDATE", dto.getOperationType());
    }

    @Test
    void should_intercept_change_state_and_publish_audit_event() {

        DocumentClass before = DocumentClass.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .name("TEST")
                .status(true)
                .build();

        when(documentClassService.findById(1L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testAuditService.changeState(1L, "ENT-1", false);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("INACTIVATE", dto.getOperationType());
    }
}
