package konkuk.corkCharge.domain.restaurant.controller;

import konkuk.corkCharge.domain.restaurant.dto.request.GetCategoryRestaurantRequest;
import konkuk.corkCharge.domain.restaurant.dto.request.GetClusterListRequest;
import konkuk.corkCharge.domain.restaurant.dto.request.GetFilterRequest;
import konkuk.corkCharge.domain.restaurant.dto.request.UserLocationRequest;
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

//    @GetMapping("/hot")
//    public BaseResponse<List<GetHotRestaurantResponse>> getHotRestaurant() {
//        return BaseResponse.ok(restaurantService.getHotRestaurants());
//    }

    @GetMapping("/filter")
    public BaseResponse<List<?>> filterRestaurant(
            @ModelAttribute GetFilterRequest request
    ) {
        return BaseResponse.ok(restaurantService.filterRestaurants(request));
    }

    @GetMapping("/map")
    public BaseResponse<List<?>> getMapCluster(
            @RequestParam(name = "level") String level,
            @RequestParam(name = "latMin") double latMin,
            @RequestParam(name = "latMax") double latMax,
            @RequestParam(name = "lonMin") double lonMin,
            @RequestParam(name = "lonMax") double lonMax
    ) {
        return BaseResponse.ok(restaurantService.GetMapCluster(level, latMin, latMax, lonMin, lonMax));
    }

    @PostMapping("/cluster/list")
    public BaseResponse<List<GetClusterListResponse>> getClusterRestaurantList(
            @RequestBody GetClusterListRequest request
    ) {
        return BaseResponse.ok(restaurantService.getClusterList(request.restaurantIds()));
    }

//    @GetMapping("/home")
//    public BaseResponse<GetHomeRestaurantResponse> getHomeRestaurant() {
//        return BaseResponse.ok(restaurantService.getHomeRestaurant());
//    }

    @PostMapping("/new")
    public BaseResponse<List<GetHomeRestaurantResponse>> getNewRestaurant(
            @RequestBody UserLocationRequest request
            ) {
        return BaseResponse.ok(restaurantService.getNewRestaurants(request));
    }

    @PostMapping("/category")
    public BaseResponse<List<GetHomeRestaurantResponse>> getCategoryRestaurants(
            @RequestBody GetCategoryRestaurantRequest request
            ) {
        return BaseResponse.ok(restaurantService.getCategoryRestaurants(request));
    }

    @PostMapping("/nearby")
    public BaseResponse<List<GetHomeRestaurantResponse>> getNearbyRestaurants(
            @RequestBody UserLocationRequest request
    ) {
        return BaseResponse.ok(restaurantService.getNearByRestaurants(request));
    }

}