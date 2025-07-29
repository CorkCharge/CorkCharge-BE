package konkuk.corkCharge.domain.suggestion.dto.response;

import java.time.LocalDateTime;

public record GetSuggestionListResponse(
    Long suggestionId,
    String title,
    LocalDateTime createdAt
) {
}
