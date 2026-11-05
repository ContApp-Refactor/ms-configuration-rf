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

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.audit.AuditAspectBank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.IBankService;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.TestBanksAuditService;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@SpringBootTest(classes = {
        AuditAspectBank.class,
        AuditEventBuilder.class,
        TestBanksAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectBanksTest {

    @Autowired
    private TestBanksAuditService testBanksAuditService;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private IBankService bankService;

    @Test
    void should_intercept_create_and_publish_audit_event() {
        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testBanksAuditService.create();

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("CREATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_update_and_publish_audit_event() {
        Bank before = new Bank();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setCode("B-001");
        before.setName("OLD");
        before.setStatus(true);

        Bank after = new Bank();
        after.setId(1L);
        after.setIdEnterprise("ENT-1");
        after.setCode("B-001");
        after.setName("NEW");
        after.setStatus(true);

        AtomicInteger counter = new AtomicInteger();
        when(bankService.findById(1L, "ENT-1"))
                .thenAnswer(invocation -> {
                    if (counter.getAndIncrement() == 0) {
                        return before;
                    }
                    return after;
                });

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        BankUpdateReq req = new BankUpdateReq();
        req.setId(1L);
        req.setIdEnterprise("ENT-1");
        req.setName("NEW");

        testBanksAuditService.update(req);

        verify(bankService, times(2)).findById(1L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("UPDATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_change_state_and_publish_audit_event() {
        Bank before = new Bank();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setCode("B-001");
        before.setName("BANK TEST");
        before.setStatus(true);

        when(bankService.findById(1L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testBanksAuditService.changeState(1L, "ENT-1", false);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("INACTIVATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_delete_and_publish_audit_event() {
        Bank before = new Bank();
        before.setId(2L);
        before.setIdEnterprise("ENT-1");
        before.setCode("B-002");
        before.setName("BANK TO DELETE");
        before.setStatus(true);

        when(bankService.findById(2L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testBanksAuditService.delete(2L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("DELETE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("2", dto.getRegisterId());
    }
}
