package konkuk.corkCharge.domain.review.dto.request;

import java.util.List;

public record GetCorkageReviewRequest(
        String keyword,
        String sido,
        String sigungu,
        List<String> dongList,
        CorkageReviewSort sort
) {
}
