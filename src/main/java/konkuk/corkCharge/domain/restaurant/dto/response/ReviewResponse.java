package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.review.domain.Review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ReviewResponse(
        String writer,
        String content,
        int rating,
        LocalDateTime createdAt,
        List<String> imageUrls
) {
    public static ReviewResponse from(Review review) {
        List<String> imageUrls = review.getImages().stream()
                .map(image -> image.getImageUrl())
                .collect(Collectors.toList());

        return new ReviewResponse(
                review.getUser().getName(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                imageUrls
        );
    }
}
