package konkuk.corkCharge.domain.notification.dto.response;

import java.util.List;

public record NotificationListResponse(
        List<NotificationListItemResponse> notifications
) {
}