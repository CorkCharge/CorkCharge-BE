package konkuk.corkCharge.domain.bookmark.dto.response;

import java.time.LocalDateTime;

public record GetSavedReviewResponse(
        Long bookmarkId,
        Long reviewId,
        String restaurantName,
        int bookmarkCount,
        String reviewImageUrl,
        int rating,
        String content,
        String userName,
        LocalDateTime createdAt
) {
}
