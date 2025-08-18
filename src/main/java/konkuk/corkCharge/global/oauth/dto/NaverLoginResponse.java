package konkuk.corkCharge.global.oauth.dto;

import konkuk.corkCharge.domain.user.domain.Role;

public record NaverLoginResponse(
        Long userId,
        Role role,
        String accessToken,
        String refreshToken
) {
    public static NaverLoginResponse of(Long userId, Role role, String accessToken, String refreshToken) {
        return new NaverLoginResponse(userId, role, accessToken, refreshToken);
    }
}
