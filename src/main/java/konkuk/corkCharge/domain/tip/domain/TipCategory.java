package konkuk.corkCharge.domain.tip.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipCategory {
    CORKAGE("콜키지 팁"),
    PAIRING("페어링 큐레이션"),
    EVENT("이벤트")
    ;

    private final String categoryName;
}
