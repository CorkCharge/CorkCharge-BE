package konkuk.corkCharge.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GetRestaurantReviewResponse(
        Long reviewId,
        String writer,
        String content,
        int rating,
        LocalDateTime createdAt,
        List<String> imageUrls,
        int getBookmarkCount
) {
}
