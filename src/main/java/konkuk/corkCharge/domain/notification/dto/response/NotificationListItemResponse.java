package konkuk.corkCharge.domain.notification.dto.response;

import konkuk.corkCharge.domain.notification.entity.Notification;
import konkuk.corkCharge.domain.notification.entity.NotificationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NotificationListItemResponse(
        Long notificationId,
        NotificationType type,
        String title,
        LocalDateTime createdAt
) {
    public static NotificationListItemResponse from(Notification notification) {
        return new NotificationListItemResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getCreatedAt()
        );
    }
}
