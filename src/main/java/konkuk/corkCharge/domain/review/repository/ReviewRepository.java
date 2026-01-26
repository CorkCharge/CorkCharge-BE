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

    // 콜키지 리뷰 필터링
    @Query(value = """
        SELECT
            rv.review_id AS reviewId,
            r.restaurant_id AS restaurantId,
            r.name AS restaurantName,
            r.address AS restaurantAddress,
            u.name AS writer,
            rv.content AS content,
            rv.rating AS rating,
            rv.created_at AS createdAt,
            COALESCE(rv.bookmark_count, 0) AS bookmarkCount
        FROM review rv
        JOIN restaurant r ON rv.restaurant_id = r.restaurant_id
        JOIN user u ON rv.user_id = u.user_id
        WHERE r.has_corkage = 1
    
          AND (
                :keyword IS NULL OR :keyword = ''
                OR r.name LIKE CONCAT('%', :keyword, '%')
                OR rv.content LIKE CONCAT('%', :keyword, '%')
          )
    
          AND (:sido IS NULL OR :sido = '' OR r.address LIKE CONCAT('%', :sido, '%'))
          AND (:sigungu IS NULL OR :sigungu = '' OR r.address LIKE CONCAT('%', :sigungu, '%'))
          AND (:dongRegex IS NULL OR :dongRegex = '' OR r.address REGEXP :dongRegex)
    
        ORDER BY
          CASE WHEN :sort = 'BOOKMARK' THEN COALESCE(rv.bookmark_count, 0) END DESC,
          rv.created_at DESC,
          rv.review_id DESC
    """, nativeQuery = true)
    List<CorkageReviewProjection> searchCorkageReviews(
            @Param("keyword") String keyword,
            @Param("sido") String sido,
            @Param("sigungu") String sigungu,
            @Param("dongRegex") String dongRegex,
            @Param("sort") String sort
    );

    // 콜키지리뷰 저장수순 top5
    @Query(value = """
        SELECT
            rv.review_id AS reviewId,
            r.restaurant_id AS restaurantId,
            r.name AS restaurantName,
            u.name AS writer,
            rv.content AS content,
            rv.rating AS rating,
            rv.created_at AS createdAt,
            COALESCE(rv.bookmark_count, 0) AS bookmarkCount
        FROM review rv
          JOIN restaurant r ON rv.restaurant_id = r.restaurant_id
          JOIN user u ON rv.user_id = u.user_id
        WHERE r.has_corkage = 1
        ORDER BY COALESCE(rv.bookmark_count, 0) DESC, rv.created_at DESC, rv.review_id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<CorkageReviewProjection> findTopCorkageReviewsOrderByBookmark(@Param("limit") int limit);

}