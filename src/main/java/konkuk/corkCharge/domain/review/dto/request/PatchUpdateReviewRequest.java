package konkuk.corkCharge.domain.review.dto.request;

public record PatchUpdateReviewRequest(
        Long userId,
        String content,
        Integer rating
) {
}
