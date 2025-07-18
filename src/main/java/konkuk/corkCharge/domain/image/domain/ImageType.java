package konkuk.corkCharge.domain.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    MAIN("가게 사진"),
    MENU("메뉴 사진"),
    USER("유저 사진")
    ;

    private final String imageType;
}
