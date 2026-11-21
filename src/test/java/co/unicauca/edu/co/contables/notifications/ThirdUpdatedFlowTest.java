package co.unicauca.edu.co.contables.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.SpringEventsThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.ThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEvent;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener.SpringEventsThirdEventListener;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener.ThirdEventListener;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@DisplayName("thirds → notifications — flujo en proceso (reemplazo de third.updated.exchange)")
class ThirdUpdatedFlowTest {

    private static final long THIRD_ID = 10L;

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private IThirdReplicaRepository thirdReplicaRepository;

    @MockitoBean
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private IThirdEventPublisher thirdEventPublisher;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("tercero natural válido - el publicador y el listener lo replican en el mismo hilo")
    void terceroNaturalValido_loReplicaElListenerEnProceso() {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());

        thirdEventPublisher.publishThirdUpdatedEvent(naturalThird());

        ThirdReplicaEntity saved = captureSaved();
        assertEquals(THIRD_ID, saved.getThirdId());
        assertEquals("ENT-10", saved.getEnterpriseId());
        assertEquals("Ana Pérez", saved.getFullName());
        assertEquals("ana@example.com", saved.getEmail());
        assertTrue(saved.getActive());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("tipo desconocido - se descarta sin tocar el repositorio ni persistir error")
    void tipoDesconocido_seDescarta() {
        ThirdUpdatedEvent event = new ThirdUpdatedEvent(producerDto());
        event.setType("SOMETHING_ELSE");

        events.publishEvent(event);

        verifyNoInteractions(thirdReplicaRepository);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Perfil por defecto - sólo el nuevo publicador y el nuevo listener están activos")
    void perfilPorDefecto_soloSpringEvents() {
        assertNotNull(applicationContext.getBean("thirdEventPublisher",
                SpringEventsThirdEventPublisher.class));
        assertNotNull(applicationContext.getBean("notificationsSpringEventsThirdListener",
                SpringEventsThirdEventListener.class));

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(ThirdEventPublisher.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(ThirdEventListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(
                        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.rabbitConfig.RabbitThirdsEventsConfig.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(
                        co.unicauca.edu.co.contables.notifications.infrastructure.config.RabbitThirdsEventsConfig.class));
    }

    private ThirdReplicaEntity captureSaved() {
        ArgumentCaptor<ThirdReplicaEntity> captor = ArgumentCaptor.forClass(ThirdReplicaEntity.class);
        verify(thirdReplicaRepository).save(captor.capture());
        return captor.getValue();
    }

    private Third naturalThird() {
        return Third.builder()
                .thId(THIRD_ID)
                .entId("ENT-10")
                .personType(ePersonType.Natural)
                .names("Ana")
                .lastNames("Pérez")
                .email("ana@example.com")
                .state(true)
                .build();
    }

    private ThirdUpdatedEventDto producerDto() {
        return ThirdUpdatedEventDto.builder()
                .thirdId(THIRD_ID)
                .entId("ENT-10")
                .fullName("Ana Pérez")
                .email("ana@example.com")
                .state(true)
                .build();
    }
}
