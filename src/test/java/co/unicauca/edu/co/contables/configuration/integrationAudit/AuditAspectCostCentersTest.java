package co.unicauca.edu.co.contables.configuration.integrationAudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

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
import co.unicauca.edu.co.contables.configuration.costCenters.domain.audit.AuditAspectCostCenters;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.TestCostCenterAuditService;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;

@SpringBootTest(classes = {
                AuditAspectCostCenters.class,
                AuditEventBuilder.class,
                TestCostCenterAuditService.class
})
@EnableAspectJAutoProxy
class AuditAspectCostCentersTest {

        @Autowired
        private TestCostCenterAuditService service;

        @MockBean
        private AuditEventPublisher auditEventPublisher;

        @MockBean
        private IJwtUtils jwtUtils;

        @MockBean
        private ICostCenterService costCenterService;

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
                assertEquals("COST_CENTERS", dto.getModuleName());
        }

        @Test
        void should_publish_update_audit() {

                CostCenter before = CostCenter.builder()
                                .id(1L)
                                .idEnterprise("ENT-1")
                                .code("OLD")
                                .name("OLD NAME")
                                .status(true)
                                .build();

                CostCenter after = CostCenter.builder()
                                .id(1L)
                                .idEnterprise("ENT-1")
                                .code("NEW")
                                .name("NEW NAME")
                                .status(true)
                                .build();

                when(costCenterService.findById(1L, "ENT-1"))
                                .thenReturn(before)
                                .thenReturn(after);

                when(jwtUtils.getId()).thenReturn("user-1");
                when(jwtUtils.getUsername()).thenReturn("juan");
                when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

                CostCenterUpdateReq req = new CostCenterUpdateReq();
                req.setId(1L);
                req.setIdEnterprise("ENT-1");
                req.setCode("NEW");
                req.setName("NEW NAME");

                service.update(req);

                ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

                verify(auditEventPublisher).publish(captor.capture());

                OperationEventDto dto = captor.getValue();

                assertEquals("UPDATE", dto.getOperationType());
                assertEquals("ENT-1", dto.getEnterpriseId());

                Map<String, Object> dataObject = dto.getDataObject();

                assertEquals(true, dataObject.containsKey("changes"));
        }

        @Test
        void should_publish_change_state_audit() {

                CostCenter before = CostCenter.builder()
                                .id(1L)
                                .idEnterprise("ENT-1")
                                .code("CC01")
                                .name("ADMIN")
                                .status(true)
                                .build();

                CostCenter after = CostCenter.builder()
                                .id(1L)
                                .idEnterprise("ENT-1")
                                .code("CC01")
                                .name("ADMIN")
                                .status(false)
                                .build();

                when(costCenterService.findById(1L, "ENT-1"))
                                .thenReturn(before)
                                .thenReturn(after);

                when(jwtUtils.getId()).thenReturn("user-1");
                when(jwtUtils.getUsername()).thenReturn("juan");
                when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));

                service.changeState(1L, "ENT-1", false);

                ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

                verify(auditEventPublisher).publish(captor.capture());

                OperationEventDto dto = captor.getValue();

                assertEquals("INACTIVATE", dto.getOperationType());
                assertEquals("ENT-1", dto.getEnterpriseId());
        }
}
