package konkuk.corkCharge.global.oauth.controller;

import konkuk.corkCharge.global.oauth.dto.NaverLoginResponse;
import konkuk.corkCharge.global.oauth.dto.TokenReissueResponse;
import konkuk.corkCharge.global.oauth.service.OAuthService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/naver/login")
    public BaseResponse<NaverLoginResponse> naverCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state") String state
    ) {
        return BaseResponse.ok(oAuthService.login(code, state));
    }

    @PostMapping("/reissue")
    public BaseResponse<TokenReissueResponse> reissue(
            @RequestHeader("Authorization") String refreshBearer
    ) {
        return BaseResponse.ok(oAuthService.reissue(refreshBearer));
    }

    // 네이버 로그인 없이 테스트 유저 생성 및 토큰 발급
    @PostMapping("/test/user")
    public BaseResponse<NaverLoginResponse> createTestUser(
            @RequestParam("name") String name
    ) {
        return BaseResponse.ok(oAuthService.createTestUser(name));
    }

}
