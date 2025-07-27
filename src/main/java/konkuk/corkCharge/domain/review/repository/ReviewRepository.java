package konkuk.corkCharge.domain.review.repository;

import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUser_UserId(Long userId);
}