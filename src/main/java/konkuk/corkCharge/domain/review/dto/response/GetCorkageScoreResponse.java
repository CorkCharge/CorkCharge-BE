package konkuk.corkCharge.domain.review.dto.response;

import konkuk.corkCharge.domain.review.domain.Review;

public record GetCorkageScoreResponse(
        Long reviewId,
        Long restaurantId,
        String restaurantName,
        String userName,
        String content,
        Integer rating,
        String createdAt,
        String imageUrl,
        int bookmarkCount
) {
    public static GetCorkageScoreResponse from(Review review, String imageUrl) {

        return new GetCorkageScoreResponse(
                review.getReviewId(),
                review.getRestaurant().getRestaurantId(),
                review.getRestaurant().getName(),
                review.getUser().getName(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt().toLocalDate().toString(),
                imageUrl,
                review.getRestaurant().getBookmarkCount()
        );
    }
}
