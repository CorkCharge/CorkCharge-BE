package konkuk.corkCharge.domain.corkageStore.controller;

import konkuk.corkCharge.domain.corkageStore.dto.request.GetCorkageFilterRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAddCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAdminCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.response.GetCorkageVerificationResponse;
import konkuk.corkCharge.domain.corkageStore.dto.response.PostAdminCorkageResponse;
import konkuk.corkCharge.domain.corkageStore.service.CorkageStoreService;
import konkuk.corkCharge.domain.restaurant.dto.response.GetSearchRestaurantResponse;
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
            @RequestBody PostAddCorkageRequest request
    ) {
        corkageStoreService.createCorkage(request);
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

    @PostMapping("/request/admin")
    public BaseResponse<PostAdminCorkageResponse> adminRequestCorkage(@RequestBody PostAdminCorkageRequest request, @RequestParam Long userId){
        return BaseResponse.ok(corkageStoreService.adminRequestCorkage(request, userId));
    }
}