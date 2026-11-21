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

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.test.util.ReflectionTestUtils;

import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.EventDtoThird;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.ThirdUpdatedEventDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("notifications ThirdEventListener Unit Tests")
class ThirdEventListenerUnitTest {

    private static final long THIRD_ID = 15L;
    private static final String ENT_ID = "ENT-15";
    private static final String FULL_NAME = "Ana Pérez";
    private static final String EMAIL = "ana@example.com";

    @Mock
    private IThirdReplicaRepository thirdReplicaRepository;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private Channel channel;

    private ThirdEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ThirdEventListener(thirdReplicaRepository, messageErrorHandlingPort);
        ReflectionTestUtils.setField(listener, "messageErrorHandlingPort", messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Réplica existente - actualiza la entidad encontrada, guarda y hace ACK")
    void replicaExistente_actualizaGuardaYAck() throws IOException {
        ThirdReplicaEntity existing = new ThirdReplicaEntity();
        existing.setId(99L);
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.of(existing));

        listener.listenToThirdsQueue(message(), validEvent(), channel, 1L);

        ThirdReplicaEntity saved = captureSaved();
        assertSame(existing, saved);
        assertEquals(THIRD_ID, saved.getThirdId());
        assertEquals(ENT_ID, saved.getEnterpriseId());
        assertEquals(FULL_NAME, saved.getFullName());
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getActive());
        verify(channel).basicAck(1L, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Réplica nueva - crea la entidad, guarda y hace ACK")
    void replicaNueva_creaGuardaYAck() throws IOException {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());

        listener.listenToThirdsQueue(message(), validEvent(), channel, 2L);

        ThirdReplicaEntity saved = captureSaved();
        assertNull(saved.getId());
        assertEquals(THIRD_ID, saved.getThirdId());
        assertEquals(ENT_ID, saved.getEnterpriseId());
        assertEquals(FULL_NAME, saved.getFullName());
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getActive());
        verify(channel).basicAck(2L, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Estado inactivo - guarda active=false")
    void estadoInactivo_guardaActiveFalse() throws IOException {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.getData().setState(false);

        listener.listenToThirdsQueue(message(), event, channel, 3L);

        assertFalse(captureSaved().getActive());
        verify(channel).basicAck(3L, false);
    }

    @Test
    @DisplayName("Tipo desconocido - descarta, hace ACK y no persiste error")
    void tipoDesconocido_descartaYAck() throws IOException {
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.setType("SOMETHING_ELSE");

        listener.listenToThirdsQueue(message(), event, channel, 4L);

        verify(thirdReplicaRepository, never()).save(any());
        verify(channel).basicAck(4L, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Tipo nulo - persiste validación y hace ACK")
    void tipoNulo_persisteValidacionYAck() throws IOException {
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.setType(null);

        listener.listenToThirdsQueue(message(), event, channel, 5L);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event type is null"),
                contains("\"thirdId\":15"),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(5L, false);
    }

    @Test
    @DisplayName("thirdId nulo - persiste validación y hace ACK")
    void thirdIdNulo_persisteValidacionYAck() throws IOException {
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.getData().setThirdId(null);

        listener.listenToThirdsQueue(message(), event, channel, 6L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: thirdId is null"),
                contains("\"thirdId\":null"),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(6L, false);
        verify(thirdReplicaRepository, never()).findByThirdId(anyLong());
    }

    @Test
    @DisplayName("entId nulo - persiste validación y hace ACK")
    void entIdNulo_persisteValidacionYAck() throws IOException {
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.getData().setEntId(null);

        listener.listenToThirdsQueue(message(), event, channel, 7L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: entId is null"),
                contains("\"entId\":null"),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(7L, false);
    }

    @Test
    @DisplayName("fullName en blanco - persiste validación y hace ACK")
    void fullNameEnBlanco_persisteValidacionYAck() throws IOException {
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.getData().setFullName("   ");

        listener.listenToThirdsQueue(message(), event, channel, 8L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: fullName is null or blank"),
                contains("\"fullName\":\"   \""),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(8L, false);
    }

    @Test
    @DisplayName("email en blanco - persiste validación y hace ACK")
    void emailEnBlanco_persisteValidacionYAck() throws IOException {
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.getData().setEmail("");

        listener.listenToThirdsQueue(message(), event, channel, 9L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: email is null or blank"),
                contains("\"email\":\"\""),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(9L, false);
    }

    @Test
    @DisplayName("state nulo - persiste validación y hace ACK")
    void stateNulo_persisteValidacionYAck() throws IOException {
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.getData().setState(null);

        listener.listenToThirdsQueue(message(), event, channel, 10L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Validation failed: state is null"),
                contains("\"state\":null"),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(10L, false);
    }

    @Test
    @DisplayName("Error al guardar la réplica - persiste procesamiento, hace ACK y no propaga")
    void errorAlGuardar_persisteProcesamientoYAck() throws IOException {
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());
        when(thirdReplicaRepository.save(any(ThirdReplicaEntity.class)))
                .thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> listener.listenToThirdsQueue(message(), validEvent(), channel, 11L));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Processing error: db down"),
                contains("\"thirdId\":15"),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(11L, false);
    }

    @Test
    @DisplayName("Error al buscar la réplica - persiste procesamiento, hace ACK y no propaga")
    void errorAlBuscar_persisteProcesamientoYAck() throws IOException {
        when(thirdReplicaRepository.findByThirdId(anyLong()))
                .thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> listener.listenToThirdsQueue(message(), validEvent(), channel, 12L));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("THIRD_UPDATED"),
                eq("Processing error: db down"),
                contains("\"thirdId\":15"),
                eq("ThirdParty.Sync"));
        verify(channel).basicAck(12L, false);
    }

    @Test
    @DisplayName("Fallo al persistir la validación - hace ACK y no propaga")
    void falloAlPersistirValidacion_haceAckYNoPropaga() throws IOException {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());
        EventDtoThird<ThirdUpdatedEventDto, String> event = validEvent();
        event.getData().setThirdId(null);

        assertDoesNotThrow(() -> listener.listenToThirdsQueue(message(), event, channel, 13L));

        verify(channel).basicAck(13L, false);
    }

    @Test
    @DisplayName("Fallo del ACK - no propaga")
    void falloAck_noPropaga() throws IOException {
        doThrow(new IOException("channel closed")).when(channel).basicAck(anyLong(), eq(false));
        when(thirdReplicaRepository.findByThirdId(THIRD_ID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> listener.listenToThirdsQueue(message(), validEvent(), channel, 14L));
    }

    private Message message() {
        return new Message(new byte[0]);
    }

    private EventDtoThird<ThirdUpdatedEventDto, String> validEvent() {
        ThirdUpdatedEventDto data = ThirdUpdatedEventDto.builder()
                .thirdId(THIRD_ID)
                .entId(ENT_ID)
                .fullName(FULL_NAME)
                .email(EMAIL)
                .state(true)
                .build();
        return new EventDtoThird<>(data, "THIRD_UPDATED");
    }

    private ThirdReplicaEntity captureSaved() {
        ArgumentCaptor<ThirdReplicaEntity> captor = ArgumentCaptor.forClass(ThirdReplicaEntity.class);
        verify(thirdReplicaRepository).save(captor.capture());
        ThirdReplicaEntity saved = captor.getValue();
        assertNotNull(saved);
        return saved;
    }
}
