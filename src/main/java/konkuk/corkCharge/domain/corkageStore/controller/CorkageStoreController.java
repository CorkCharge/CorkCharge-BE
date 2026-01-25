package konkuk.corkCharge.domain.corkageStore.controller;

import konkuk.corkCharge.domain.corkageStore.dto.request.PostAddCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAdminCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.response.GetCorkageVerificationResponse;
import konkuk.corkCharge.domain.corkageStore.dto.response.PostAdminCorkageResponse;
import konkuk.corkCharge.domain.corkageStore.service.CorkageStoreService;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/corkages")
@RequiredArgsConstructor
public class CorkageStoreController {

    private final CorkageStoreService corkageStoreService;

    @PostMapping
    public BaseResponse<Void> createCorkage(
            @LoginUserId Long userId,
            @RequestBody PostAddCorkageRequest request
    ) {
        corkageStoreService.createCorkage(userId, request);
        return BaseResponse.ok(null);
    }

    @GetMapping("/verify")
    public BaseResponse<List<GetCorkageVerificationResponse>> requestCorkage(
            @LoginUserId Long userId
    ) {
        return BaseResponse.ok(corkageStoreService.requestCorkage(userId));
    }

    @PostMapping("/request/admin")
    public BaseResponse<PostAdminCorkageResponse> adminRequestCorkage(
            @RequestBody PostAdminCorkageRequest request,
            @LoginUserId Long userId
    ){
        return BaseResponse.ok(corkageStoreService.adminRequestCorkage(request, userId));
    }
}