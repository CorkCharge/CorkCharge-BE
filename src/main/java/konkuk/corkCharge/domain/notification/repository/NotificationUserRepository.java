package konkuk.corkCharge.domain.notification.repository;

import konkuk.corkCharge.domain.notification.entity.NotificationUser;
import konkuk.corkCharge.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {

    List<NotificationUser> findByUserAndNotification_CreatedAtAfterOrderByNotification_CreatedAtDesc(
            User user,
            LocalDateTime from
    );
}