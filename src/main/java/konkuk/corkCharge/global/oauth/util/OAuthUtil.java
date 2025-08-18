package konkuk.corkCharge.global.oauth.util;

public class OAuthUtil {
    public static String buildNaverTokenRequestBody(String code, String state,
                                                    String clientId, String clientSecret, String redirectUri) {
        return "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri
                + "&code=" + code
                + "&state=" + state;
    }
}
