package konkuk.corkCharge.domain.notification.dto.response;

import konkuk.corkCharge.domain.notification.entity.Notification;
import konkuk.corkCharge.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationDetailResponse(
        Long notificationId,
        NotificationType type,
        String title,
        String content,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
    public static NotificationDetailResponse from(
            Notification notification,
            List<String> imageUrls
    ) {
        return new NotificationDetailResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                imageUrls,
                notification.getCreatedAt()
        );
    }
}