package konkuk.corkCharge.domain.notification.dto.request;

import konkuk.corkCharge.domain.notification.entity.NotificationType;

public record PostTestNotificationRequest(
        NotificationType type,
        String title,
        String content,
        Long targetUserId,   // null이면 전체 유저
        Boolean sendToAll    // true면 전체 유저
) {
}