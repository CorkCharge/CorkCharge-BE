package konkuk.corkCharge.domain.restaurant.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long reviewId,
        String writer,
        String content,
        int rating,
        LocalDateTime createdAt,
        List<String> imageUrls,
        int savedCount
) {
}
