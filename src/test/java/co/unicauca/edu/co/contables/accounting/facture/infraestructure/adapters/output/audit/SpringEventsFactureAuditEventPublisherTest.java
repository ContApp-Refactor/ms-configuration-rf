package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.DocumentEventDto;

@ExtendWith(MockitoExtension.class)
class SpringEventsFactureAuditEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsFactureAuditEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsFactureAuditEventPublisher(eventPublisher);
    }

    @Test
    @DisplayName("publish - debe publicar el DTO en el bus en proceso")
    void publish_publicaEvento() {
        DocumentEventDto dto = buildDto();

        publisher.publish(dto);

        verify(eventPublisher).publishEvent(dto);
    }

    @Test
    @DisplayName("publish - no debe propagar excepción si el bus falla")
    void publish_errorBus_noLanzaExcepcion() {
        DocumentEventDto dto = buildDto();
        doThrow(new RuntimeException("bus caído"))
                .when(eventPublisher).publishEvent(dto);

        assertDoesNotThrow(() -> publisher.publish(dto));
    }

    @Test
    @DisplayName("publish - debe publicar exactamente una vez")
    void publish_publicaExactamenteUnaVez() {
        DocumentEventDto dto = buildDto();

        publisher.publish(dto);

        verify(eventPublisher, times(1)).publishEvent(any(DocumentEventDto.class));
    }

    private DocumentEventDto buildDto() {
        return DocumentEventDto.builder()
                .enterpriseId("ENT-001")
                .documentId("1")
                .documentCode("FAC-1")
                .documentType("SALE")
                .documentDate(LocalDate.now())
                .userId("user-123")
                .userName("jdoe")
                .userRoles(List.of("Administrador"))
                .operationType("CREATE")
                .thirdPartyId("10")
                .thirdPartyName("Cliente 10")
                .moduleName("FACTURE")
                .operationAt(Instant.now())
                .documentData(Map.<String, Object>of("header", Map.of("factCode", 1L)))
                .build();
    }
}
