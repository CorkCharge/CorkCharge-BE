package konkuk.corkCharge.domain.restaurant.controller;

import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantListResponse;
import konkuk.corkCharge.domain.restaurant.service.RestaurantService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public BaseResponse<List<GetRestaurantListResponse>> getCorkageRestaurants() {
        return BaseResponse.ok(restaurantService.getCorkageRestaurants());
    }

}