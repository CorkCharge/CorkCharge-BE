package konkuk.corkCharge.domain.review.dto.response;

public record GetMyPageReviewResponse(
        String restaurantName,
        String location,
        String thumbnailUrl
) {
}
