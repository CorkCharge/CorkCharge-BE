package konkuk.corkCharge.domain.notification.controller;

import konkuk.corkCharge.domain.notification.dto.request.PostTestNotificationRequest;
import konkuk.corkCharge.domain.notification.dto.response.NotificationDetailResponse;
import konkuk.corkCharge.domain.notification.dto.response.NotificationListResponse;
import konkuk.corkCharge.domain.notification.service.NotificationService;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public BaseResponse<NotificationListResponse> getMyNotifications(
            @LoginUserId Long userId
    ) {
        return BaseResponse.ok(
                notificationService.getMyNotifications(userId)
        );
    }

    @GetMapping("/{notificationId}")
    public BaseResponse<NotificationDetailResponse> getNotificationDetail(
            @PathVariable Long notificationId
    ) {
        return BaseResponse.ok(
                notificationService.getNotificationDetail(notificationId)
        );
    }

    @PostMapping
    public BaseResponse<Void> createTestNotification( // 테스트용
            @RequestBody PostTestNotificationRequest request
    ) {
        notificationService.createTestNotification(request);
        return BaseResponse.ok(null);
    }
}