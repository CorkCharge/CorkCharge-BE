package konkuk.corkCharge.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GetCorkageReviewResponse(
        Long reviewId,
        Long restaurantId,
        String restaurantName,
        String writer,
        String content,
        int rating,
        LocalDateTime createdAt,
        List<String> imageUrls,
        int bookmarkCount,
        boolean scrap
) {
}
