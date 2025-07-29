package konkuk.corkCharge.domain.suggestion.dto.response;

import java.time.LocalDateTime;

public record GetSuggestionDetailResponse(
        String title,
        String content,
        LocalDateTime createdAt
) {
}
