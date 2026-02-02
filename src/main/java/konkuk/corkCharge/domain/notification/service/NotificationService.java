package konkuk.corkCharge.domain.notification.service;

import konkuk.corkCharge.domain.notification.dto.request.PostTestNotificationRequest;
import konkuk.corkCharge.domain.notification.dto.response.NotificationListItemResponse;
import konkuk.corkCharge.domain.notification.dto.response.NotificationListResponse;
import konkuk.corkCharge.domain.notification.entity.Notification;
import konkuk.corkCharge.domain.notification.entity.NotificationUser;
import konkuk.corkCharge.domain.notification.repository.NotificationRepository;
import konkuk.corkCharge.domain.notification.repository.NotificationUserRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationUserRepository notificationUserRepository;

    public NotificationListResponse getMyNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(USER_NOT_FOUND));

        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        List<NotificationUser> notificationUsers =
                notificationUserRepository
                        .findByUserAndNotification_CreatedAtAfterOrderByNotification_CreatedAtDesc(
                                user,
                                twoWeeksAgo
                        );

        List<NotificationListItemResponse> notifications =
                notificationUsers.stream()
                        .map(nu -> NotificationListItemResponse.from(nu.getNotification()))
                        .toList();

        return new NotificationListResponse(notifications);
    }

    public void createTestNotification(PostTestNotificationRequest request) {

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .type(request.type())
                        .title(request.title())
                        .content(request.content())
                        .build()
        );

        if (Boolean.TRUE.equals(request.sendToAll())) {
            List<User> users = userRepository.findAll();

            List<NotificationUser> notificationUsers = users.stream()
                    .map(user -> NotificationUser.builder()
                            .notification(notification)
                            .user(user)
                            .build())
                    .toList();

            notificationUserRepository.saveAll(notificationUsers);
            return;
        }

        User targetUser = userRepository.findById(request.targetUserId())
                .orElseThrow();

        notificationUserRepository.save(
                NotificationUser.builder()
                        .notification(notification)
                        .user(targetUser)
                        .build()
        );
    }
}