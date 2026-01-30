package konkuk.corkCharge.domain.notification.service;

import konkuk.corkCharge.domain.notification.dto.NotificationListItemResponse;
import konkuk.corkCharge.domain.notification.dto.NotificationListResponse;
import konkuk.corkCharge.domain.notification.entity.Notification;
import konkuk.corkCharge.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationListResponse getNotifications() {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        List<Notification> notifications =
                notificationRepository.findByCreatedAtAfterOrderByCreatedAtDesc(twoWeeksAgo);

        List<NotificationListItemResponse> result = notifications.stream()
                .map(NotificationListItemResponse::from)
                .toList();

        return new NotificationListResponse(result);
    }
}