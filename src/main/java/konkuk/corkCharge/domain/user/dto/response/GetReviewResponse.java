package konkuk.corkCharge.domain.user.dto.response;

import java.time.LocalDateTime;

public record GetReviewResponse(
        Long reviewId,
        Long restaurantId,
        Long userId,
        String content,
        int rating,
        String reviewImageUrl,
        LocalDateTime createdAt
) {
}
