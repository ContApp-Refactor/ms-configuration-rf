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

import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TestTaxesAuditService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.audit.AuditAspectTaxes;

@SpringBootTest(classes = {
        AuditAspectTaxes.class,
        AuditEventBuilder.class,
        TestTaxesAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectTaxesTest {

    @Autowired
    private TestTaxesAuditService testTaxesAuditService;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private ITaxSearchOutputPort taxSearchOutputPort;

    @Test
    void should_intercept_create_and_publish_audit_event() {
        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testTaxesAuditService.create();

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("CREATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_update_and_publish_audit_event() {
        Tax before = new Tax();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setCode("T-001");
        before.setDescription("OLD");
        before.setStatus(true);

        Tax after = new Tax();
        after.setId(1L);
        after.setIdEnterprise("ENT-1");
        after.setCode("T-001");
        after.setDescription("NEW");
        after.setStatus(true);

        AtomicInteger counter = new AtomicInteger();
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(1L, "ENT-1"))
                .thenAnswer(invocation -> {
                    if (counter.getAndIncrement() == 0) {
                        return before;
                    }
                    return after;
                });

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        TaxDTO dtoReq = new TaxDTO();
        dtoReq.setIdEnterprise("ENT-1");
        dtoReq.setDescription("NEW");

        testTaxesAuditService.update(dtoReq, 1L);

        verify(taxSearchOutputPort, times(2))
                .getTaxByIdAndEnterprise(1L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("UPDATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_change_state_and_publish_audit_event() {
        Tax before = new Tax();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setCode("T-001");
        before.setDescription("IVA 19%");
        before.setStatus(true);

        when(taxSearchOutputPort.getTaxByIdAndEnterprise(1L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testTaxesAuditService.changeState(1L, "ENT-1", false);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("INACTIVATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_delete_and_publish_audit_event() {
        Tax before = new Tax();
        before.setId(2L);
        before.setIdEnterprise("ENT-1");
        before.setCode("T-002");
        before.setDescription("TO DELETE");
        before.setStatus(true);

        when(taxSearchOutputPort.getTaxByIdAndEnterprise(2L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testTaxesAuditService.deleteByCode(2L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("DELETE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("2", dto.getRegisterId());
    }
}
