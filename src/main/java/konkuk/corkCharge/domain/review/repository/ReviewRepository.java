package konkuk.corkCharge.domain.review.repository;

import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUser_UserId(Long userId);

    @Query("SELECT r " +
            "FROM Review r " +
            "JOIN FETCH r.restaurant res " +
            "JOIN FETCH r.user u " +
            "WHERE r.createdAt >= :from")
    List<Review> findRecentReviews(@Param("from") LocalDateTime from);

    List<Review> findByRestaurant_RestaurantId(Long restaurantId);

    List<Review> findAllByRestaurant_RestaurantId(Long restaurantId);
}