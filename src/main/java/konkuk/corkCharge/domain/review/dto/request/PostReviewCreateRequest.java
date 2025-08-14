package konkuk.corkCharge.domain.review.dto.request;

public record PostReviewCreateRequest(
        String content,
        int rating
) { }
