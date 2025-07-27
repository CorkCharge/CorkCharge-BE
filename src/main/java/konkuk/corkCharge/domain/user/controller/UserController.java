package konkuk.corkCharge.domain.user.controller;

import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.domain.user.service.UserService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public BaseResponse<GetUserProfileResponse> getUserProfile(@RequestParam Long userId){
        return BaseResponse.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/modify")
    public BaseResponse<Void> editUserProfile(@RequestParam(name="userId") Long userId, @RequestParam(name="name") String name, @RequestPart(required = false) MultipartFile image){
        userService.updateUserProfile(userId, name, image);
        return BaseResponse.ok(null);
    }
}
