package konkuk.corkCharge.domain.restaurant.controller;

import konkuk.corkCharge.domain.restaurant.dto.request.GetFilterRequest;
import konkuk.corkCharge.domain.restaurant.dto.response.*;
import konkuk.corkCharge.domain.restaurant.service.RestaurantService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    ) {
        return BaseResponse.ok(restaurantService.getRestaurantDetail(restaurantId));
    }

    @GetMapping("/search")
    public BaseResponse<List<GetSearchRestaurantResponse>> searchRestaurant(
            @RequestParam(name = "keyword") String keyword
    ) {
        return BaseResponse.ok(restaurantService.searchRestaurants(keyword));
    }

    @GetMapping("/hot")
    public BaseResponse<List<GetHotRestaurantResponse>> getHotRestaurant() {
        return BaseResponse.ok(restaurantService.getHotRestaurants());
    }

    @GetMapping("/filter")
    public BaseResponse<List<?>> filterRestaurant(
            @ModelAttribute GetFilterRequest request
    ) {
        return BaseResponse.ok(restaurantService.filterRestaurants(request));
    }

}