package konkuk.corkCharge.domain.review.repository;

import java.time.LocalDateTime;

public interface CorkageReviewProjection {
    Long getReviewId();
    Long getRestaurantId();
    String getRestaurantName();
    String getRestaurantAddress();
    String getWriter();
    String getContent();
    Integer getRating();
    LocalDateTime getCreatedAt();
    Integer getBookmarkCount();
}
