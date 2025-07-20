package konkuk.corkCharge.domain.restaurant.controller;

import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantDetailResponse;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantListResponse;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantMapResponse;
import konkuk.corkCharge.domain.restaurant.service.RestaurantService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/map")
    public BaseResponse<List<GetRestaurantMapResponse>> getCorkageMap() {
        return BaseResponse.ok(restaurantService.getRestaurantMap());
    }

    @GetMapping("/{restaurantId}")
    public BaseResponse<GetRestaurantDetailResponse> getRestaurantDetail(
            @PathVariable(name = "restaurantId") Long restaurantId
    ){
        return BaseResponse.ok(restaurantService.getRestaurantDetail(restaurantId));
    }

}