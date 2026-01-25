package konkuk.corkCharge.domain.user.controller;

import konkuk.corkCharge.domain.user.dto.request.PostRoleRequest;
import konkuk.corkCharge.domain.user.dto.response.*;
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
            @RequestParam(name="name") String name
    ){
        userService.updateUserProfile(userId, name);
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
    public BaseResponse<Void> updateUserRole(
            @LoginUserId Long userId,
            @RequestBody PostRoleRequest request
    ){
        userService.updateRoleAndNickname(userId, request.role(), request.nickname());
        return BaseResponse.ok(null);
    }

    @PutMapping("/registration")
    public BaseResponse<Void> updateRegistration(
            @LoginUserId Long userId,
            @RequestPart MultipartFile image
    ){
        userService.updateRegistration(userId, image);
        return BaseResponse.ok(null);
    }

    @GetMapping("/helprequests")
    public BaseResponse<GetMyHelpRequestsResponse> getMyHelpRequests(
            @LoginUserId Long userId
    ) {
        return BaseResponse.ok(userService.getMyHelpRequests(userId));
    }

    @GetMapping("/helprequests/{helprequestId}")
    public BaseResponse<GetMyHelpRequestDetailResponse> getMyHelpRequestDetail(
            @LoginUserId Long userId,
            @PathVariable Long helprequestId
    ) {
        return BaseResponse.ok(userService.getMyHelpRequestDetail(userId, helprequestId));
    }

}