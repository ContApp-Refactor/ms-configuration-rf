package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @brief Endpoint SSE para recibir notificaciones en vivo.
 *
 * El cliente abre un EventSource hacia este endpoint y recibe eventos
 * "notification" con el payload de la notificacion en JSON.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationStreamController {

    private final SseEmitterRegistry sseEmitterRegistry;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String userId) {
        return sseEmitterRegistry.register(userId);
    }
}
