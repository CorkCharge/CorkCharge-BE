package konkuk.corkCharge.domain.restaurant.service;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantDetailResponse;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantListResponse;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantMapResponse;
import konkuk.corkCharge.domain.restaurant.dto.response.GetSearchRestaurantResponse;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.api.naverMapsApi.NaverGeocodingClient;
import konkuk.corkCharge.global.api.naverMapsApi.dto.Address;
import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.CORKAGE_RESTAURANT_NOT_FOUND;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.RESTAURANT_NOT_FOUND;

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

}