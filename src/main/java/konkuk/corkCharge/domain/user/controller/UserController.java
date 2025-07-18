package konkuk.corkCharge.domain.user.controller;

import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.dto.response.UserProfileResponseDto;
import konkuk.corkCharge.domain.user.service.UserService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public BaseResponse<UserProfileResponseDto> getUserProfile(@RequestParam Long userId){
        UserProfileResponseDto userProfile = userService.getUserProfile(userId);
        return BaseResponse.ok(userProfile);
    }
}
