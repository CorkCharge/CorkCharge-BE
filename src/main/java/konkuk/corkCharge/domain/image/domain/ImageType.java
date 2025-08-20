package konkuk.corkCharge.domain.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    MAIN("가게 사진"),
    MENU("메뉴 사진"),
    PROFILE("프로필 사진"),
    REGISTRATION_IMAGE("사업자 등록증 사진");
    private final String imageType;
}
