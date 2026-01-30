package konkuk.corkCharge.domain.helpRequest.controller;

import jakarta.validation.Valid;
import konkuk.corkCharge.domain.helpRequest.dto.request.GetHelpRequestRestaurantsRequest;
import konkuk.corkCharge.domain.helpRequest.dto.request.PostHelpRequestDetailRequest;
import konkuk.corkCharge.domain.helpRequest.dto.response.GetHelpRequestRestaurantsResponse;
import konkuk.corkCharge.domain.helpRequest.service.HelpRequestService;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
public class HelpRequestController {
    private final HelpRequestService helpRequestService;

    @PostMapping("/{restaurantId}")
    public BaseResponse<Void> createHelpRequest(
            @LoginUserId Long userId,
            @PathVariable Long restaurantId
    ) {
        helpRequestService.createHelpRequest(userId, restaurantId);
        return BaseResponse.ok(null);
    }

    @PostMapping("/detail")
    public BaseResponse<Void> submitHelpRequestDetail(
            @LoginUserId Long userId,
            @Valid @RequestBody PostHelpRequestDetailRequest request
    ) {
        helpRequestService.submitDetail(userId, request);
        return BaseResponse.ok(null);
    }

    @PostMapping("/restaurants")
    public BaseResponse<GetHelpRequestRestaurantsResponse> getHelpRequestRestaurants(
            @RequestBody(required = false) GetHelpRequestRestaurantsRequest request
    ) {
        return BaseResponse.ok(
                helpRequestService.getHelpRequestRestaurants(request)
        );
    }
}
