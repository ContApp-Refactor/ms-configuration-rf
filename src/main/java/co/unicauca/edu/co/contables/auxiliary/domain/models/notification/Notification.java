package co.unicauca.edu.co.contables.auxiliary.domain.models.notification;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ENotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * @brief Modelo de dominio de una notificacion dirigida a un usuario.
 *
 * Representa un evento (ej. reporte programado listo para descarga) que
 * el backend envia al usuario. Contiene un identificador de referencia
 * opaco (por ejemplo, executionId) para que el frontend pueda accionar
 * sobre el recurso asociado.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private UUID notificationId;
    private String userId;
    private ENotificationType type;
    private String title;
    private String message;
    private UUID referenceId;
    private String referencePublicId;
    private boolean read;
    private Instant createdAt;
    private Instant readAt;
}
