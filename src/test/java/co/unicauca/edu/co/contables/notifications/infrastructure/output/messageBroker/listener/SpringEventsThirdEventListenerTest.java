package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEvent;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("notifications SpringEventsThirdEventListener Unit Tests")
class SpringEventsThirdEventListenerTest {

    private static final long THIRD_ID = 15L;
    private static final String ENT_ID = "ENT-15";
    private static final String FULL_NAME = "Ana Pérez";
    private static final String EMAIL = "ana@example.com";

    @Mock
    private IThirdReplicaRepository thirdReplicaRepository;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsThirdEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsThirdEventListener(thirdReplicaRepository, messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Réplica existente - actualiza la entidad encontrada y la guarda")
    void replicaExistente_actualizaYGuarda() {
        ThirdReplicaEntity existing = new ThirdReplicaEntity();
        existing.setId(99L);
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.of(existing));

        listener.onThirdUpdated(new ThirdUpdatedEvent(producerDto()));

        ThirdReplicaEntity saved = captureSaved();
        assertSame(existing, saved);
        assertEquals(THIRD_ID, saved.getThirdId());
        assertEquals(ENT_ID, saved.getEnterpriseId());
        assertEquals(FULL_NAME, saved.getFullName());
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getActive());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Réplica nueva - crea la entidad con todos los campos y la guarda")
    void replicaNueva_creaYGuarda() {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());

        listener.onThirdUpdated(new ThirdUpdatedEvent(producerDto()));

        ThirdReplicaEntity saved = captureSaved();
        assertNull(saved.getId());
        assertEquals(THIRD_ID, saved.getThirdId());
        assertEquals(ENT_ID, saved.getEnterpriseId());
        assertEquals(FULL_NAME, saved.getFullName());
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getActive());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Tipo desconocido - se descarta sin tocar el repositorio ni persistir error")
    void tipoDesconocido_seDescarta() {
        ThirdUpdatedEvent event = new ThirdUpdatedEvent(producerDto());
        event.setType("SOMETHING_ELSE");

        listener.onThirdUpdated(event);

        verifyNoInteractions(thirdReplicaRepository);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Evento nulo - persiste validación con tipo unknown")
    void eventoNulo_persisteValidacion() {
        assertDoesNotThrow(() -> listener.onThirdUpdated(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("unknown"),
                eq("Validation failed: Event is null"),
                eq("{\"error\": \"Event is null\"}"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("Data nula - persiste validación con el tipo del evento")
    void dataNula_persisteValidacion() {
        listener.onThirdUpdated(new ThirdUpdatedEvent(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: Event data is null"),
                eq("{\"error\": \"Event data is null\", \"eventType\": \"THIRD_UPDATED\"}"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("Tipo nulo - persiste validación")
    void tipoNulo_persisteValidacion() {
        ThirdUpdatedEvent event = new ThirdUpdatedEvent(producerDto());
        event.setType(null);

        listener.onThirdUpdated(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event type is null"),
                contains("\"thirdId\":15"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("thirdId nulo - persiste validación")
    void thirdIdNulo_persisteValidacion() {
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setThirdId(null);

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: thirdId is null"),
                contains("\"thirdId\":null"),
                eq("ThirdParty.Sync"));
        verifyNoInteractions(thirdReplicaRepository);
    }

    @Test
    @DisplayName("entId nulo - persiste validación")
    void entIdNulo_persisteValidacion() {
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setEntId(null);

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: entId is null"),
                contains("\"entId\":null"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("fullName nulo - persiste validación")
    void fullNameNulo_persisteValidacion() {
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setFullName(null);

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: fullName is null or blank"),
                contains("\"fullName\":null"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("fullName en blanco - persiste validación")
    void fullNameEnBlanco_persisteValidacion() {
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setFullName("   ");

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: fullName is null or blank"),
                contains("\"fullName\":\"   \""),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("email nulo - persiste validación")
    void emailNulo_persisteValidacion() {
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setEmail(null);

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: email is null or blank"),
                contains("\"email\":null"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("email en blanco - persiste validación")
    void emailEnBlanco_persisteValidacion() {
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setEmail("");

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: email is null or blank"),
                contains("\"email\":\"\""),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("state nulo - persiste validación")
    void stateNulo_persisteValidacion() {
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setState(null);

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: state is null"),
                contains("\"state\":null"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("Estado inactivo - la réplica guarda active=false")
    void estadoInactivo_guardaActiveFalse() {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setState(false);

        listener.onThirdUpdated(new ThirdUpdatedEvent(dto));

        assertFalse(captureSaved().getActive());
    }

    @Test
    @DisplayName("Error al guardar la réplica - persiste procesamiento y no propaga")
    void errorAlGuardar_persisteProcesamientoYNoPropaga() {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());
        when(thirdReplicaRepository.save(any(ThirdReplicaEntity.class)))
                .thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> listener.onThirdUpdated(new ThirdUpdatedEvent(producerDto())));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Processing error: db down"),
                contains("\"thirdId\":15"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("Error al buscar la réplica - persiste procesamiento y no propaga")
    void errorAlBuscar_persisteProcesamientoYNoPropaga() {
        when(thirdReplicaRepository.findByThirdId(anyLong()))
                .thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> listener.onThirdUpdated(new ThirdUpdatedEvent(producerDto())));

        verify(thirdReplicaRepository, never()).save(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Processing error: db down"),
                contains("\"thirdId\":15"),
                eq("ThirdParty.Sync"));
    }

    @Test
    @DisplayName("Fallo al persistir la validación - no propaga")
    void falloAlPersistirValidacion_noPropaga() {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());
        co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto dto =
                producerDto();
        dto.setThirdId(null);

        assertDoesNotThrow(() -> listener.onThirdUpdated(new ThirdUpdatedEvent(dto)));
    }

    @Test
    @DisplayName("Fallo al persistir el procesamiento - no propaga")
    void falloAlPersistirProcesamiento_noPropaga() {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());
        when(thirdReplicaRepository.save(any(ThirdReplicaEntity.class)))
                .thenThrow(new RuntimeException("db down"));
        doThrow(new RuntimeException("error db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onThirdUpdated(new ThirdUpdatedEvent(producerDto())));
    }

    private ThirdReplicaEntity captureSaved() {
        ArgumentCaptor<ThirdReplicaEntity> captor = ArgumentCaptor.forClass(ThirdReplicaEntity.class);
        verify(thirdReplicaRepository).save(captor.capture());
        ThirdReplicaEntity saved = captor.getValue();
        assertNotNull(saved);
        return saved;
    }

    private co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto producerDto() {
        return co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto.builder()
                .thirdId(THIRD_ID)
                .entId(ENT_ID)
                .fullName(FULL_NAME)
                .email(EMAIL)
                .state(true)
                .build();
    }
}
