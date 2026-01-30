package konkuk.corkCharge.domain.notification.dto;

import java.util.List;

public record NotificationListResponse(
        List<NotificationListItemResponse> notifications
) {
}