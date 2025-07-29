package konkuk.corkCharge.domain.suggestion.dto.request;

import konkuk.corkCharge.domain.suggestion.domain.SuggestionCategory;


public record PostSuggestionRequest (
        String title,
        String content,
        SuggestionCategory category
){

}
