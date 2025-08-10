package konkuk.corkCharge.domain.corkageStore.controller;

import konkuk.corkCharge.domain.corkageStore.dto.request.GetCorkageFilterRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAddCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.response.GetCorkageVerificationResponse;
import konkuk.corkCharge.domain.corkageStore.service.CorkageStoreService;
import konkuk.corkCharge.domain.restaurant.dto.response.GetSearchRestaurantResponse;
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

    @PostMapping("/filter")
    public BaseResponse<List<GetSearchRestaurantResponse>> filterCorkageStores(
            @RequestBody GetCorkageFilterRequest request
    ) {
        return BaseResponse.ok(corkageStoreService.filterCorkageStores(request));
    }

    @GetMapping("/verify")
    public BaseResponse<List<GetCorkageVerificationResponse>> requestCorkage(@RequestParam Long userId){
        return BaseResponse.ok(corkageStoreService.requestCorkage(userId));
    }
}