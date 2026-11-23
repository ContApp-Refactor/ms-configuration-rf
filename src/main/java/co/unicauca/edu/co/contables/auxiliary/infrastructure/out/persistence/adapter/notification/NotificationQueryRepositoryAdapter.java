package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.notification;

import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.notification.INotificationQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.notification.NotificationEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.notification.INotificationPersistenceMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.notification.INotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationQueryRepositoryAdapter implements INotificationQueryRepositoryPort {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final INotificationRepository notificationRepository;
    private final INotificationPersistenceMapper notificationPersistenceMapper;

    @Override
    public List<Notification> findByUserId(String userId, Boolean unreadOnly, Integer limit) {
        Pageable pageable = PageRequest.of(0, resolveLimit(limit));
        List<NotificationEntity> entities = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return entities.stream().map(notificationPersistenceMapper::toDomain).toList();
    }

    @Override
    public long countUnread(String userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    @Override
    public Optional<Notification> findByIdAndUserId(UUID notificationId, String userId) {
        return notificationRepository.findByNotificationIdAndUserId(notificationId, userId)
                .map(notificationPersistenceMapper::toDomain);
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }
}
