package konkuk.corkCharge.domain.notification.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.notification.dto.request.PostTestNotificationRequest;
import konkuk.corkCharge.domain.notification.dto.response.NotificationDetailResponse;
import konkuk.corkCharge.domain.notification.dto.response.NotificationListItemResponse;
import konkuk.corkCharge.domain.notification.dto.response.NotificationListResponse;
import konkuk.corkCharge.domain.notification.entity.Notification;
import konkuk.corkCharge.domain.notification.entity.NotificationType;
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

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public NotificationDetailResponse getNotificationDetail(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()->new CustomException(NOTIFICATION_NOT_FOUND));

        List<String> imageUrls =
                imageRepository.findUrlsByCategoryAndTypeId(
                        ImageCategory.NOTIFICATION,
                        notificationId
                );

        return NotificationDetailResponse.from(notification, imageUrls);
    }

    @Transactional
    public void createTestNotification(PostTestNotificationRequest request) {
    // Todo 테스트 전용 기능이므로 예외처리가 꼼꼼하게 되어 있지 않습니다.
        if (!NotificationType.isValid(request.type())) {
            throw new CustomException(NOTIFICATION_TYPE_NOT_FOUND);
        }

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
                .orElseThrow(()->new CustomException(USER_NOT_FOUND));

        notificationUserRepository.save(
                NotificationUser.builder()
                        .notification(notification)
                        .user(targetUser)
                        .build()
        );
    }
}