package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @brief Registro de {@link SseEmitter} por usuario.
 *
 * Permite mantener varias conexiones (pestanias multiples) por usuario y
 * difundir un mismo evento a todas. Maneja completion/timeout/error
 * removiendo emitters automaticamente para evitar fugas.
 */
@Component
@Slf4j
public class SseEmitterRegistry {

    private static final long DEFAULT_TIMEOUT_MS = 30L * 60L * 1000L;

    private final Map<String, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter register(String userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        emittersByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(userId, emitter);
        });
        emitter.onError(ex -> {
            log.debug("SSE emitter error for user {}: {}", userId, ex.getMessage());
            remove(userId, emitter);
        });

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ignored) {
            remove(userId, emitter);
        }
        return emitter;
    }

    public void sendToUser(String userId, String eventName, Object data) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception ex) {
                log.debug("Failed to push SSE event to user {}: {}", userId, ex.getMessage());
                remove(userId, emitter);
            }
        }
    }

    private void remove(String userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersByUser.remove(userId);
            }
        }
    }
}
