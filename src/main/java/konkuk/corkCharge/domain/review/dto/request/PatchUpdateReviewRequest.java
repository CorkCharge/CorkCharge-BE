package konkuk.corkCharge.domain.review.dto.request;

public record PatchUpdateReviewRequest(
        String content,
        Integer rating
) {
}
