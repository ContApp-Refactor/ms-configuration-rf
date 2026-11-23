package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.notification;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.notification.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<NotificationEntity> findByUserIdAndReadOrderByCreatedAtDesc(String userId, boolean read, Pageable pageable);

    long countByUserIdAndRead(String userId, boolean read);

    Optional<NotificationEntity> findByNotificationIdAndUserId(UUID notificationId, String userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt " +
            "WHERE n.notificationId = :notificationId AND n.userId = :userId AND n.read = false")
    int markAsRead(@Param("notificationId") UUID notificationId,
                   @Param("userId") String userId,
                   @Param("readAt") Instant readAt);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt " +
            "WHERE n.userId = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") String userId, @Param("readAt") Instant readAt);

    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.notificationId = :notificationId AND n.userId = :userId")
    int deleteByNotificationIdAndUserId(@Param("notificationId") UUID notificationId, @Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.userId = :userId AND n.read = true")
    int deleteAllReadByUserId(@Param("userId") String userId);
}
