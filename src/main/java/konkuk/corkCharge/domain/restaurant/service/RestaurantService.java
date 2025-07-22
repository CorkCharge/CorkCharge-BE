package konkuk.corkCharge.domain.restaurant.service;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.request.GetFilterRequest;
import konkuk.corkCharge.domain.restaurant.dto.response.*;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.api.naverMapsApi.NaverGeocodingClient;
import konkuk.corkCharge.global.api.naverMapsApi.dto.Address;
import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final NaverGeocodingClient naverGeocodingClient;

    @Transactional(readOnly = true)
    public List<GetRestaurantListResponse> getCorkageRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByHasCorkageTrue();

        if (restaurants.isEmpty()) {
            throw new CustomException(CORKAGE_RESTAURANT_NOT_FOUND);
        }

        return restaurants.stream()
                .map(GetRestaurantListResponse::from)
                .toList();
    }

    @Transactional
    public List<GetRestaurantMapResponse> getRestaurantMap() {
        List<Restaurant> restaurants = restaurantRepository.findByHasCorkageTrue();

        if (restaurants.isEmpty()) {
            throw new CustomException(CORKAGE_RESTAURANT_NOT_FOUND);
        }

        return restaurants.stream()
                .map(restaurant -> {
                    if (restaurant.getLatitude() == 0 && restaurant.getLongitude() == 0) {
                        NaverMapsResponse response = naverGeocodingClient.getCoordinatesByAddress(restaurant.getAddress());
                        if (!response.addresses().isEmpty()) {
                            Address address = response.addresses().get(0);
                            restaurant.updateCoordinates(
                                    Double.parseDouble(address.latitude()),
                                    Double.parseDouble(address.longitude())
                            );
                        }
                    }

                    String corkagePrice = restaurant.getCorkageStore() != null ? restaurant.getCorkageStore().getCorkagePrice() : null;
                    return GetRestaurantMapResponse.of(restaurant, corkagePrice);
                })
                .toList();
    }

    public GetRestaurantDetailResponse getRestaurantDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        return GetRestaurantDetailResponse.from(restaurant);
    }

    @Transactional
    public List<GetSearchRestaurantResponse> searchRestaurants(String keyword) {
        List<Restaurant> matchedRestaurants = restaurantRepository.findByNameContaining(keyword);

        return matchedRestaurants.stream()
                .map(GetSearchRestaurantResponse::from)
                .toList();
    }

    @Transactional
    public List<GetHotRestaurantResponse> getHotRestaurants() {
        List<Restaurant> hotRestaurants = restaurantRepository.findByBookmarkCountGreaterThanEqual(5);

        return hotRestaurants.stream()
                .map(GetHotRestaurantResponse::from)
                .toList();
    }

    @Transactional
    public List<?> filterRestaurants(GetFilterRequest request) {
        List<Restaurant> matchedRestaurants = filterByAddress(request.sido(), request.sigungu(), request.dongList());

        return switch (request.type()) {
            case "hot" -> matchedRestaurants.stream()
                    .filter(r -> r.getBookmarkCount() >= 5)
                    .map(GetHotRestaurantResponse::from)
                    .toList();

            case "map" -> matchedRestaurants.stream()
                    .filter(Restaurant::isHasCorkage)
                    .map(GetSearchRestaurantResponse::from)
                    .toList();

            default -> throw new CustomException(NOT_EXIT_TYPE);
        };

    }

    private List<Restaurant> filterByAddress(String sido, String sigungu, List<String> dongList) {
        if (sido == null || sido.isBlank()) {
            throw new CustomException(SIDO_REQUIRED);
        }

        List<Restaurant> matchedRestaurants = restaurantRepository.findByAddressContaining(sido);

        if (sigungu != null && !sigungu.isBlank()) {
            matchedRestaurants = matchedRestaurants.stream()
                    .filter(r -> r.getAddress().contains(sigungu))
                    .toList();
        }

        if (dongList != null && !dongList.isEmpty()) {
            matchedRestaurants = matchedRestaurants.stream()
                    .filter(r -> dongList.stream().anyMatch(d -> r.getAddress().contains(d)))
                    .toList();
        }

        return matchedRestaurants;
    }

}