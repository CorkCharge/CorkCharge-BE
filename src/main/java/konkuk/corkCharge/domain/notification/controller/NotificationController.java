package konkuk.corkCharge.domain.notification.controller;

import konkuk.corkCharge.domain.notification.dto.NotificationListResponse;
import konkuk.corkCharge.domain.notification.service.NotificationService;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}