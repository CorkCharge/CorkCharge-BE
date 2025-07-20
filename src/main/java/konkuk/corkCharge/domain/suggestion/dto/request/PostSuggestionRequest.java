package konkuk.corkCharge.domain.suggestion.dto.request;

public record PostSuggestionRequest (
        Long restaurantId,
        String content,
        String category
){

}
