package co.unicauca.edu.co.contables.accounting.catalogue.integrationAudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.TestCatalogueAuditService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.audit.AuditAspectAccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@SpringBootTest(classes = {
        AuditAspectAccountCatalogue.class,
        AuditEventBuilder.class,
        TestCatalogueAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectCatalogueTest {

    @Autowired
    private TestCatalogueAuditService testAuditService;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private IAccountCatalogueSearchOutputPort accountCatalogueServiceSearch;

    @Test
    void should_intercept_create_and_publish_audit_event() {

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testAuditService.create();

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher)
                .publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("CREATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_update_and_publish_audit_event() {
        AccountCatalogue before = new AccountCatalogue();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setCode("100");
        before.setDescription("OLD");
        before.setStatus(true);
        AccountCatalogue after = new AccountCatalogue();
        after.setId(1L);
        after.setIdEnterprise("ENT-1");
        after.setCode("100");
        after.setDescription("NEW");
        after.setStatus(true);
        AtomicInteger counter = new AtomicInteger();
        when(accountCatalogueServiceSearch
                .getAccountCatalogueByIdAndIdEnterprise(1L, "ENT-1"))
                .thenAnswer(invocation -> {
                    if (counter.getAndIncrement() == 0) {
                        return before;
                    }
                    return after;
                });

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));
        AccountCatalogue request = new AccountCatalogue();
        request.setIdEnterprise("ENT-1");
        request.setDescription("NEW");
        testAuditService.update(1L, request);
        verify(accountCatalogueServiceSearch, times(2))
                .getAccountCatalogueByIdAndIdEnterprise(1L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("UPDATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_change_state_and_publish_audit_event() {

        AccountCatalogue before = new AccountCatalogue();

        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setCode("100");
        before.setDescription("TEST");
        before.setStatus(true);

        when(accountCatalogueServiceSearch
                .getAccountCatalogueByIdAndIdEnterprise(1L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testAuditService.changeState(1L, "ENT-1", false);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher)
                .publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("INACTIVATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
    }
}
