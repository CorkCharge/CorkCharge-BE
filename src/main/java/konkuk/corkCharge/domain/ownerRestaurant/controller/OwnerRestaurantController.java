package konkuk.corkCharge.domain.ownerRestaurant.controller;

import konkuk.corkCharge.domain.ownerRestaurant.dto.response.GetOwnerMyRestaurantListResponse;
import konkuk.corkCharge.domain.ownerRestaurant.service.OwnerRestaurantService;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.exception.CustomException;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ownerRestaurant")
@RequiredArgsConstructor
public class OwnerRestaurantController {

    private final OwnerRestaurantService ownerRestaurantService;

    @PostMapping("/{restaurantId}")
    public BaseResponse<Void> registerRestaurant(
            @LoginUserId Long userId,
            @PathVariable(name = "restaurantId") Long restaurantId
    ){
        ownerRestaurantService.registerRestaurant(userId, restaurantId);
        return BaseResponse.ok(null);
    }

    @GetMapping("/my")
    public BaseResponse<GetOwnerMyRestaurantListResponse> getMyRestaurants(
            @LoginUserId Long userId
    ) {
        return BaseResponse.ok(ownerRestaurantService.getMyRestaurants(userId));
    }

}
