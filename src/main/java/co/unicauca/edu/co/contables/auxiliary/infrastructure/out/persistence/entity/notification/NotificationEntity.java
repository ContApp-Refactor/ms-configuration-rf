package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.notification;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ENotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * @brief Entidad JPA para una notificacion entregable a un usuario.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "NOTIFICATION",
        indexes = {
                @Index(name = "idx_notification_user_unread", columnList = "userId, read, createdAt")
        }
)
public class NotificationEntity {

    @Id
    private UUID notificationId;

    @Column(nullable = false, length = 128)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ENotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private UUID referenceId;

    @Column(length = 64)
    private String referencePublicId;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant readAt;
}
