package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.dto.response.ReviewResponse;
import konkuk.corkCharge.domain.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.REVIEW;

@Component
@RequiredArgsConstructor
public class ReviewResponseMapper {

    private final ImageRepository imageRepository;

    public ReviewResponse toResponse(Review review) {

        List<String> imageUrls = imageRepository
                .findUrlsByCategoryAndTypeId(REVIEW, review.getReviewId());

        return new ReviewResponse(
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
