package co.unicauca.edu.co.contables.inventory.products.integrationAudit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.aspect.AuditAspect;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@SpringBootTest(classes = {
                AuditAspect.class,
                AuditEventBuilder.class,
                TestInventoryAuditService.class
})
@EnableAspectJAutoProxy
public class AuditAspectIntegrationTest {

        @MockBean
        private AuditEventPublisher auditEventPublisher;

        @MockBean
        private IJwtUtils jwtUtils;

        @MockBean
        private IProductPersistencePort productPersistencePort;

        @MockBean
        private ICategoryPersistencePort categoryPersistencePort;

        @MockBean
        private IProductTypePersistencePort productTypePersistencePort;

        @MockBean
        private IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;

        @Autowired
        private TestInventoryAuditService service;

        @BeforeEach
        void setUp() {
                when(jwtUtils.getId()).thenReturn("user-1");
                when(jwtUtils.getUsername()).thenReturn("juan");
                when(jwtUtils.getRealmRoles()).thenReturn(List.of("ADMIN"));
        }

        @Test
        @DisplayName("Debe interceptar CREATE y publicar evento")
        void should_intercept_create_and_publish_event() {

                service.createCategory();

                ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

                verify(auditEventPublisher).publish(captor.capture());

                OperationEventDto dto = captor.getValue();

                assertAll(
                                () -> assertEquals("CREATE", dto.getOperationType()),
                                () -> assertEquals("CATEGORY", dto.getAffectedTable()),
                                () -> assertEquals("ENT-1", dto.getEnterpriseId()),
                                () -> assertEquals("1", dto.getRegisterId()),
                                () -> assertNotNull(dto.getDataObject()));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Debe interceptar UPDATE y generar diff")
        void should_intercept_update_and_generate_diff() {

                Product before = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .name("OLD")
                                .description("OLD DESC")
                                .state(true)
                                .build();

                Product after = Product.builder()
                                .id(1L)
                                .enterpriseId("ENT-1")
                                .name("NEW")
                                .description("NEW DESC")
                                .state(true)
                                .build();

                when(productPersistencePort.findByIdAndEnterpriseId(1L, "ENT-1"))
                                .thenReturn(Optional.of(before))
                                .thenReturn(Optional.of(after));

                Product updateRequest = Product.builder()
                                .name("NEW")
                                .description("NEW DESC")
                                .build();
                service.updateProduct(1L, updateRequest, "ENT-1");
                ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);
                verify(auditEventPublisher).publish(captor.capture());
                OperationEventDto dto = captor.getValue();
                Map<String, Object> changes = (Map<String, Object>) dto.getDataObject().get("changes");

                assertAll(
                                () -> assertEquals("UPDATE", dto.getOperationType()),
                                () -> assertTrue(changes.containsKey("name")),
                                () -> assertTrue(changes.containsKey("description")));
        }

        @Test
        @DisplayName("Debe interceptar DELETE y publicar evento")
        void should_intercept_delete_and_publish_event() {

                ProductType before = ProductType.builder()
                                .id(5L)
                                .enterpriseId("ENT-1")
                                .name("TYPE")
                                .state(true)
                                .build();

                when(productTypePersistencePort.findByIdAndEnterpriseId(5L, "ENT-1"))
                                .thenReturn(Optional.of(before));

                service.deleteProductType(5L, "ENT-1");

                ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

                verify(auditEventPublisher).publish(captor.capture());

                OperationEventDto dto = captor.getValue();

                assertAll(
                                () -> assertEquals("DELETE", dto.getOperationType()),
                                () -> assertEquals("PRODUCT_TYPE", dto.getAffectedTable()));
        }

        @Test
        @DisplayName("Debe resolver ACTIVATE cuando estado anterior era false")
        void should_resolve_activate_operation() {

                UnitOfMeasure before = UnitOfMeasure.builder()
                                .id(10L)
                                .enterpriseId("ENT-1")
                                .name("KG")
                                .abbreviation("KG")
                                .state(false)
                                .build();

                when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(10L, "ENT-1"))
                                .thenReturn(Optional.of(before));

                service.changeUnitState(10L, "ENT-1");

                ArgumentCaptor<OperationEventDto> captor = ArgumentCaptor.forClass(OperationEventDto.class);

                verify(auditEventPublisher).publish(captor.capture());

                OperationEventDto dto = captor.getValue();

                assertEquals("ACTIVATE", dto.getOperationType());
        }
}
