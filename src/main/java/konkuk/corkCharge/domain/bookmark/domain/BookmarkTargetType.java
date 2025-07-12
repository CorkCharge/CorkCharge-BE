package konkuk.corkCharge.domain.bookmark.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookmarkTargetType {
    REVIEW("리뷰"),
    RESTAURANT("매장"),
    TIP("팁")
    ;

    private final String categoryName;
}
