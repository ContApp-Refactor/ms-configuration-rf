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

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.audit.AuditAspectBankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.TestBankAccountAuditService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@SpringBootTest(classes = {
        AuditAspectBankAccount.class,
        AuditEventBuilder.class,
        TestBankAccountAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectBankAccountTest {

    @Autowired
    private TestBankAccountAuditService testService;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private IBankAccountService bankAccountService;

    @Test
    void should_intercept_create_and_publish_audit_event() {
        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testService.create();

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("CREATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_update_and_publish_audit_event() {
        BankAccount before = new BankAccount();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setAccountNumber(123456L);
        before.setStatus(true);

        BankAccount after = new BankAccount();
        after.setId(1L);
        after.setIdEnterprise("ENT-1");
        after.setAccountNumber(999999L);
        after.setStatus(true);

        AtomicInteger counter = new AtomicInteger();
        when(bankAccountService.findById(1L, "ENT-1"))
                .thenAnswer(invocation -> {
                    if (counter.getAndIncrement() == 0) {
                        return before;
                    }
                    return after;
                });

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        BankAccountUpdateReq req = new BankAccountUpdateReq();
        req.setId(1L);
        req.setIdEnterprise("ENT-1");
        req.setAccountNumber(999999L);

        testService.update(req);

        verify(bankAccountService, times(2)).findById(1L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("UPDATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_change_state_and_publish_audit_event() {
        BankAccount before = new BankAccount();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setAccountNumber(123456L);
        before.setStatus(true);

        when(bankAccountService.findById(1L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testService.changeState(1L, "ENT-1", false);

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("INACTIVATE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("1", dto.getRegisterId());
    }

    @Test
    void should_intercept_delete_and_publish_audit_event() {
        BankAccount before = new BankAccount();
        before.setId(2L);
        before.setIdEnterprise("ENT-1");
        before.setAccountNumber(123456L);
        before.setStatus(true);

        when(bankAccountService.findById(2L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testService.delete(2L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("DELETE", dto.getOperationType());
        assertEquals("ENT-1", dto.getEnterpriseId());
        assertEquals("2", dto.getRegisterId());
    }
}
