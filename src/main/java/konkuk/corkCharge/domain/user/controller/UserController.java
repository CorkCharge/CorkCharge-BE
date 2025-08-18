package konkuk.corkCharge.domain.user.controller;

import konkuk.corkCharge.domain.user.domain.Role;
import konkuk.corkCharge.domain.user.dto.request.PostRoleRequest;
import konkuk.corkCharge.domain.user.dto.response.GetMyPageResponse;
import konkuk.corkCharge.domain.user.dto.response.GetReviewResponse;
import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.domain.user.service.UserService;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public BaseResponse<GetUserProfileResponse> getUserProfile(
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/modify")
    public BaseResponse<Void> editUserProfile(
            @LoginUserId Long userId,
            @RequestParam(name="name") String name,
            @RequestPart(required = false) MultipartFile image
    ){
        userService.updateUserProfile(userId, name, image);
        return BaseResponse.ok(null);
    }

    @GetMapping("/reviews")
    public BaseResponse<List<GetReviewResponse>> getUserReviews(
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(userService.getUserReviews(userId));
    }

    @DeleteMapping
    public BaseResponse<Void> deleteUser(
            @LoginUserId Long userId
    ){
        userService.deleteUser(userId);
        return BaseResponse.ok(null);
    }

    @GetMapping("/page")
    public BaseResponse<GetMyPageResponse> getMyPage(
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(userService.getMyPage(userId));
    }

    @PutMapping("/role")
    public BaseResponse<Role> updateUserRole(
            @LoginUserId Long userId,
            @RequestBody PostRoleRequest request
    ){
        return BaseResponse.ok(userService.updateUserRole(userId, request.role()));
    }
}