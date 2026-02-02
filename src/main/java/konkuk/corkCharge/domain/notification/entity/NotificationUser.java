package konkuk.corkCharge.domain.notification.entity;

import jakarta.persistence.*;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(
        name = "notification_user",
        indexes = {
                @Index(name = "idx_notification_user_user", columnList = "user_id"),
                @Index(name = "idx_notification_user_notification", columnList = "notification_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}