package konkuk.corkCharge.global.oauth.service;

import jakarta.transaction.Transactional;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import konkuk.corkCharge.global.oauth.dto.NaverLoginResponse;
import konkuk.corkCharge.global.oauth.dto.NaverTokenResponse;
import konkuk.corkCharge.global.oauth.dto.NaverUserResponse;
import konkuk.corkCharge.global.oauth.dto.TokenReissueResponse;
import konkuk.corkCharge.global.oauth.entity.RefreshToken;
import konkuk.corkCharge.global.oauth.jwt.JwtProvider;
import konkuk.corkCharge.global.oauth.jwt.JwtUtil;
import konkuk.corkCharge.global.oauth.repository.RefreshTokenRepository;
import konkuk.corkCharge.global.oauth.util.OAuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService extends DefaultOAuth2UserService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUri;

    private final RestClient restClient = RestClient.builder().build();

    @Transactional
    public NaverLoginResponse login(String code, String state) {
        NaverTokenResponse token = requestToken(code, state);
        if (token.error() != null) {
            throw new CustomException(NAVER_TOKEN_ERROR);
        }

        User user = registerOrLogin(token.accessToken());

        String accessToken = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        refreshTokenRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.of(user.getUserId(), refreshToken))
                );

        return NaverLoginResponse.of(user.getUserId(), user.getRole(), accessToken, refreshToken);
    }

    private NaverTokenResponse requestToken(String code, String state) {
        String body = OAuthUtil.buildNaverTokenRequestBody(code, state, clientId, clientSecret, redirectUri);

        return restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        (req, res) -> {
                            String errorBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            log.error("❌ Naver 토큰 요청 실패: {}", errorBody);
                            throw new CustomException(NAVER_TOKEN_REQUEST_FAILED);
                        })
                .body(NaverTokenResponse.class);
    }

    private User registerOrLogin(String accessToken) {
        NaverUserResponse naverUser = getUserInfo(accessToken);
        var res = (naverUser == null) ? null : naverUser.response();
        if (res == null) {
            throw new CustomException(NAVER_USER_INFO_REQUEST_FAILED);
        }

        String socialId = "naver " + res.id();

        return userRepository.findBySocialId(socialId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .socialId(socialId)
                            .name(res.name())
                            .email(res.email())
                            .build();
                    return userRepository.save(newUser);
                });
    }

    private NaverUserResponse getUserInfo(String accessToken) {
        return restClient.get()
                .uri(userInfoUri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .body(NaverUserResponse.class);
    }

    @Transactional
    public TokenReissueResponse reissue(String bearerToken) {
        String refreshToken = extractBearerToken(bearerToken);

        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new CustomException(JWT_INVALID);
        }

        Long userId = jwtUtil.extractUserIdFromToken(refreshToken);

        RefreshToken saved = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(JWT_REFRESH_NOT_FOUND));

        if (!saved.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(JWT_REFRESH_TOKEN_MISMATCH);
        }

        String newAccess = jwtProvider.generateAccessToken(userId);
        String newRefresh = jwtProvider.generateRefreshToken(userId);
        saved.updateToken(newRefresh);
        refreshTokenRepository.save(saved);

        return TokenReissueResponse.of(newAccess, newRefresh);
    }

    private String extractBearerToken(String header) {
        if (header == null)
            return "";

        String prefix = "Bearer ";
        return header.startsWith(prefix) ? header.substring(prefix.length()).trim() : header.trim();
    }

    @Transactional
    public NaverLoginResponse createTestUser(String name) {
        String socialId = "test " + name;

        User user = userRepository.findBySocialId(socialId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(name)
                                .email(name + "@naver.com")
                                .socialId(socialId)
                                .build()
                ));

        String accessToken = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        refreshTokenRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.of(user.getUserId(), refreshToken))
                );

        return NaverLoginResponse.of(user.getUserId(), user.getRole(), accessToken, refreshToken);
    }

}
