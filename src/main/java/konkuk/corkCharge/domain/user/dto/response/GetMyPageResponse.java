package konkuk.corkCharge.domain.user.dto.response;

import konkuk.corkCharge.domain.review.dto.response.GetMyPageReviewResponse;

import java.util.List;

public record GetMyPageResponse(
        String nickname,
        String socialId,
        List<GetMyPageReviewResponse> reviews
) {
}
