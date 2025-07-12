package konkuk.corkCharge.domain.suggestion.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuggestionCategory {
    CORKAGE_ERROR("콜키지 정보 오류"),
    OTHER_ERROR("기타 정보 오류")
    ;

    private final String categoryName;
}
