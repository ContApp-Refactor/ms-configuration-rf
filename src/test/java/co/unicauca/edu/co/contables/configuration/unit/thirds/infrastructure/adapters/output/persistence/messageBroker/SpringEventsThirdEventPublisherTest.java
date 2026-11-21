package co.unicauca.edu.co.contables.configuration.unit.thirds.infrastructure.adapters.output.persistence.messageBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.SpringEventsThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEvent;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("thirds SpringEventsThirdEventPublisher Unit Tests")
class SpringEventsThirdEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SpringEventsThirdEventPublisher publisher;

    @Test
    @DisplayName("Persona natural - publica el nombre consolidado y todos los campos")
    void personaNatural_publicaNombreConsolidado() {
        publisher.publishThirdUpdatedEvent(naturalThird());

        ThirdUpdatedEvent event = captureEvent();
        ThirdUpdatedEventDto dto = event.getData();

        assertEquals(10L, dto.getThirdId());
        assertEquals("ENT-10", dto.getEntId());
        assertEquals("Ana Pérez", dto.getFullName());
        assertEquals("ana@example.com", dto.getEmail());
        assertTrue(dto.getState());
    }

    @Test
    @DisplayName("Persona jurídica - publica la razón social como fullName")
    void personaJuridica_publicaRazonSocial() {
        publisher.publishThirdUpdatedEvent(juridicaThird());

        ThirdUpdatedEventDto dto = captureEvent().getData();

        assertEquals(20L, dto.getThirdId());
        assertEquals("ENT-20", dto.getEntId());
        assertEquals("Acme SAS", dto.getFullName());
        assertEquals("contacto@acme.com", dto.getEmail());
        assertTrue(dto.getState());
    }

    @Test
    @DisplayName("Contrato - publica un ThirdUpdatedEvent concreto con tipo THIRD_UPDATED")
    void contrato_publicaEventoConcretoConTipo() {
        publisher.publishThirdUpdatedEvent(naturalThird());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        Object published = captor.getValue();
        assertInstanceOf(ThirdUpdatedEvent.class, published);
        ThirdUpdatedEvent event = (ThirdUpdatedEvent) published;
        assertEquals("THIRD_UPDATED", event.getType());
        assertEquals(ThirdUpdatedEvent.TYPE, event.getType());
    }

    private ThirdUpdatedEvent captureEvent() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return (ThirdUpdatedEvent) captor.getValue();
    }

    private Third naturalThird() {
        return Third.builder()
                .thId(10L)
                .entId("ENT-10")
                .personType(ePersonType.Natural)
                .names("Ana")
                .lastNames("Pérez")
                .email("ana@example.com")
                .state(true)
                .build();
    }

    private Third juridicaThird() {
        return Third.builder()
                .thId(20L)
                .entId("ENT-20")
                .personType(ePersonType.Juridica)
                .socialReason("Acme SAS")
                .email("contacto@acme.com")
                .state(true)
                .build();
    }
}
