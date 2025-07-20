package konkuk.corkCharge.domain.review.dto.request;

public record PostReviewCreateRequest(
        Long userId,
        String content,
        int rating
) { }
