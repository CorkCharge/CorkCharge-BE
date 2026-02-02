package konkuk.corkCharge.domain.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    MAIN("가게 사진"),
    MENU("메뉴 사진"),
    NOTIFICATION_CONTENT("알림 본문 이미지");
    private final String imageType;
}
