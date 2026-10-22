package co.unicauca.edu.co.contables.accounting.catalogue.integrationAudit;

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

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.audit.AuditAspectPayMethods;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.TestPaymentMethodsAuditService;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;

@SpringBootTest(classes = {
        AuditAspectPayMethods.class,
        AuditEventBuilder.class,
        TestPaymentMethodsAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectPayMethodsTest {

    @Autowired
    private TestPaymentMethodsAuditService testAuditService;

    @MockBean
    private AuditEventPublisher auditEventPublisher;

    @MockBean
    private IJwtUtils jwtUtils;

    @MockBean
    private IPaymentMethodService paymentMethodService;

    @Test
    void should_intercept_create_and_publish_audit_event() {

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

        AccountCatalogue account = new AccountCatalogue();
        account.setId(100L);

        PaymentMethod before = new PaymentMethod();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setName("OLD");
        before.setAccountingAccountEntity(account);
        before.setAccountingAccount("1105");
        before.setStatus(true);

        PaymentMethod after = new PaymentMethod();
        after.setId(1L);
        after.setIdEnterprise("ENT-1");
        after.setName("NEW");
        after.setAccountingAccountEntity(account);
        after.setAccountingAccount("1105");
        after.setStatus(true);

        when(paymentMethodService.findById(1L, "ENT-1"))
                .thenReturn(before)
                .thenReturn(after);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        PaymentMethodUpdateReq req = new PaymentMethodUpdateReq();
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
    void should_intercept_inactivate_and_publish_audit_event() {

        AccountCatalogue account = new AccountCatalogue();
        account.setId(100L);

        PaymentMethod before = new PaymentMethod();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setName("TRANSFER");
        before.setAccountingAccountEntity(account);
        before.setAccountingAccount("1105");
        before.setStatus(true);

        when(paymentMethodService.findById(1L, "ENT-1"))
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

    @Test
    void should_intercept_delete_and_publish_audit_event() {

        AccountCatalogue account = new AccountCatalogue();
        account.setId(100L);

        PaymentMethod before = new PaymentMethod();
        before.setId(1L);
        before.setIdEnterprise("ENT-1");
        before.setName("TRANSFER");
        before.setAccountingAccountEntity(account);
        before.setAccountingAccount("1105");
        before.setStatus(true);

        when(paymentMethodService.findById(1L, "ENT-1"))
                .thenReturn(before);

        when(jwtUtils.getId()).thenReturn("user-1");
        when(jwtUtils.getUsername()).thenReturn("juan");
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

        testAuditService.delete(1L, "ENT-1");

        ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

        verify(auditEventPublisher).publish(captor.capture());

        OperationEventDto dto = captor.getValue();

        assertEquals("DELETE", dto.getOperationType());
    }
}
