package konkuk.corkCharge.domain.corkageStore.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OptionType {
    GLASS_PROVIDED("잔 제공"),
    ICE_PROVIDED("얼음 제공"),
    ONE_BOTTLE_FREE("한 병 무료"),
    TWO_BOTTLE_FREE("두 병 무료"),
    WINE_GLASS_PROVIDED("와인잔 제공"),
    ETC("여러 기타");

    private final String label;

    @Override
    public String toString() {
        return label;
    }
}