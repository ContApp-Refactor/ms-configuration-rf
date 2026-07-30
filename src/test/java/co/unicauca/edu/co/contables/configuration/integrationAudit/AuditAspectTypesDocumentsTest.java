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

import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.configuration.commons.security.IJwtUtils;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.audit.AuditAspectTypesDocuments;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeService;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.TestTypesDocumentsAuditService;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;

@SpringBootTest(classes = {
        AuditAspectTypesDocuments.class,
        AuditEventBuilder.class,
        TestTypesDocumentsAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectTypesDocumentsTest {

    @Autowired
    private TestTypesDocumentsAuditService service;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private IDocumentTypeService documentTypeService;

    @Test
    void should_publish_create_audit() {

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        service.create();

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("CREATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
    }

    @Test
    void should_publish_update_audit() {

        DocumentType before = DocumentType.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .name("OLD")
                .prefix("OLD")
                .documentClassId(10L)
                .moduleId(1)
                .status(true)
                .build();

        DocumentType after = DocumentType.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .name("NEW")
                .prefix("NEW")
                .documentClassId(10L)
                .moduleId(1)
                .status(true)
                .build();

        when(documentTypeService.findById(1L, "ENT-1"))
                .thenReturn(before)
                .thenReturn(after);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        DocumentTypeUpdateReq req = new DocumentTypeUpdateReq();
        req.setId(1L);
        req.setIdEnterprise("ENT-1");
        req.setName("NEW");
        req.setPrefix("NEW");
        req.setDocumentClassId(10L);
        req.setModuleId(1);

        service.update(req);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("UPDATE", dto.getOperationType());
    }

    @Test
    void should_publish_change_state_audit() {

        DocumentType before = DocumentType.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .status(true)
                .build();

        DocumentType after = DocumentType.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .status(false)
                .build();

        when(documentTypeService.findById(1L, "ENT-1"))
                .thenReturn(before)
                .thenReturn(after);

        when(jwtUtils.getId()).thenReturn("user-1");

        service.changeState(1L, "ENT-1", false);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("INACTIVATE", dto.getOperationType());
    }

}
