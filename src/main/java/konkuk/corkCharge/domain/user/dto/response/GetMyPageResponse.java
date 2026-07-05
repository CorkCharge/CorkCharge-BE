package konkuk.corkCharge.domain.user.dto.response;

import konkuk.corkCharge.domain.review.dto.response.GetHomeCorkageReviewResponse;

import java.util.List;

public record GetMyPageResponse(
        String nickname,
        String email,
        List<GetHomeCorkageReviewResponse> reviews
) {
}