package konkuk.corkCharge.domain.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageCategory {
    RESTAURANT("매장"),
    REVIEW("리뷰"),
    TIP("팁"),
    CORKAGE("콜키지"),
    USER("유저"),
    NOTIFICATION("알림")
    ;

    private final String imageCategory;
}
