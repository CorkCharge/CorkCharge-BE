package konkuk.corkCharge.global.oauth.dto;

public record TokenReissueResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenReissueResponse of(String accessToken, String refreshToken) {
        return new TokenReissueResponse(accessToken, refreshToken);
    }
}
