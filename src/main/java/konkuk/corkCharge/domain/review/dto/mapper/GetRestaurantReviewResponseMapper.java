package konkuk.corkCharge.domain.review.dto.mapper;

import konkuk.corkCharge.domain.review.dto.response.GetRestaurantReviewResponse;
import konkuk.corkCharge.domain.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetRestaurantReviewResponseMapper {

    public GetRestaurantReviewResponse toResponse(Review review, List<String> imageUrls) {
        return new GetRestaurantReviewResponse(
                review.getReviewId(),
                review.getUser().getName(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                imageUrls,
                review.getBookmarkCount() == null ? 0 : review.getBookmarkCount()
        );
    }
}
