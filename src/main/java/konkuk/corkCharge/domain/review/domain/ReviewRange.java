package konkuk.corkCharge.domain.review.domain;

import konkuk.corkCharge.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.INVALID_RANGE;

@Getter
@RequiredArgsConstructor
public enum ReviewRange {
    ONE_DAY("1", 1),
    THREE_DAYS("3", 3),
    ONE_WEEK("7", 7),
    ONE_MONTH("30", 30)
    ;

    private final String key;
    private final int dyas;

    public static ReviewRange fromValue(String value) {
        for (ReviewRange range : values()) {
            if (range.key.equals(value))
                return range;
        }
        throw new CustomException(INVALID_RANGE);
    }

    public LocalDateTime getFromDate() {
        return LocalDateTime.now().minusDays(dyas);
    }

}
