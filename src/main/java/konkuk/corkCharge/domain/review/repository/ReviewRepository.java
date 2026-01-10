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

    List<Review> findByRestaurant_RestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<Review> findAllByRestaurant_RestaurantId(Long restaurantId);

    // 최신순
    @Query("""
        select
            rv.reviewId as reviewId,
            r.restaurantId as restaurantId,
            r.name as restaurantName,
            u.name as writer,
            rv.content as content,
            rv.rating as rating,
            rv.createdAt as createdAt,
            coalesce(rv.bookmarkCount, 0) as bookmarkCount
        from Review rv
          join rv.restaurant r
          join rv.user u
        where r.hasCorkage = true
        order by rv.createdAt desc, rv.reviewId desc
    """)
    List<CorkageReviewProjection> findAllCorkageReviewsOrderByLatest();

    // 저장수순
    @Query("""
        select
            rv.reviewId as reviewId,
            r.restaurantId as restaurantId,
            r.name as restaurantName,
            u.name as writer,
            rv.content as content,
            rv.rating as rating,
            rv.createdAt as createdAt,
            coalesce(rv.bookmarkCount, 0) as bookmarkCount
        from Review rv
          join rv.restaurant r
          join rv.user u
        where r.hasCorkage = true
        order by coalesce(rv.bookmarkCount, 0) desc,
                 rv.createdAt desc,
                 rv.reviewId desc
    """)
    List<CorkageReviewProjection> findAllCorkageReviewsOrderByBookmark();

}