package co.unicauca.edu.co.contables.configuration.integrationAudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.audit.AuditAspectNoCommerTag;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.models.Tag;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services.ITagService;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services.TestNoCommercialTagAuditService;

@SpringBootTest(classes = {
        AuditAspectNoCommerTag.class,
        AuditEventBuilder.class,
        TestNoCommercialTagAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectNoCommercialTagTest {

    @Autowired
    private TestNoCommercialTagAuditService service;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private ITagService tagService;

    @Test
    void should_publish_create_audit() {

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        Tag tag = Tag.builder()
                .id(1L)
                .enterpriseId("ENT-1")
                .title("TAG")
                .description("DESC")
                .build();

        service.create(tag);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("CREATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
    }

    @Test
    void should_publish_update_audit() {

        Tag before = Tag.builder()
                .id(1L)
                .enterpriseId("ENT-1")
                .title("OLD")
                .description("OLD DESC")
                .build();

        Tag after = Tag.builder()
                .id(1L)
                .enterpriseId("ENT-1")
                .title("NEW")
                .description("NEW DESC")
                .build();

        when(tagService.getTag(1L, "ENT-1"))
                .thenReturn(Optional.of(before))
                .thenReturn(Optional.of(after));

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        Tag update = Tag.builder()
                .enterpriseId("ENT-1")
                .title("NEW")
                .description("NEW DESC")
                .build();

        service.update(1L, update);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("UPDATE", dto.getOperationType());
    }

    @Test
    void should_publish_delete_audit() {

        Tag before = Tag.builder()
                .id(1L)
                .enterpriseId("ENT-1")
                .title("TAG")
                .description("DESC")
                .build();

        when(tagService.getTagById(1L))
                .thenReturn(Optional.of(before));

        when(jwtUtils.getId()).thenReturn("user-1");

        service.delete(1L);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("DELETE", dto.getOperationType());
        assertEquals("1", dto.getRegisterId());
    }
}
