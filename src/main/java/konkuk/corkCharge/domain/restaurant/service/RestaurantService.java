package konkuk.corkCharge.domain.restaurant.service;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
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

    @Transactional
    public List<?> GetMapCluster(String level, double latMin, double latMax, double lonMin, double lonMax) {
        List<Restaurant> restaurants = restaurantRepository.findByHasCorkageTrue();

        if (restaurants.isEmpty()) {
            throw new CustomException(CORKAGE_RESTAURANT_NOT_FOUND);
        }

        // 위도/경도가 없는 매장의 경우 추가
        restaurants.forEach(restaurant -> {
            if (restaurant.getLatitude() == 0 || restaurant.getLongitude() == 0) {
                NaverMapsResponse response = naverGeocodingClient.getCoordinatesByAddress(restaurant.getAddress());
                if (!response.addresses().isEmpty()) {
                    Address address = response.addresses().get(0);
                    restaurant.updateCoordinates(
                            Double.parseDouble(address.latitude()),
                            Double.parseDouble(address.longitude())
                    );
                }
            }
        });

        List<Restaurant> filtered = restaurants.stream()
                .filter(r -> r.getLatitude() >= latMin && r.getLatitude() <= latMax)
                .filter(r -> r.getLongitude() >= lonMin && r.getLongitude() <= lonMax)
                .toList();

        return switch (level) {
            case "restaurant" -> filtered.stream()
                    .map(GetMapRestaurantResponse::from)
                    .toList();

            case "dong", "sigungu", "sido" -> filtered.stream()
                    .map(GetMapClusterResponse::from)
                    .toList();

            default -> throw new CustomException(BAD_REQUEST);
        };
    }

    @Transactional(readOnly = true)
    public List<GetClusterListResponse> getClusterList(List<Long> restaurantIds) {
        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);

        return restaurants.stream()
                .sorted((r1, r2) -> {
                    int price1 = getComparableCorkagePrice(r1);
                    int price2 = getComparableCorkagePrice(r2);
                    return Integer.compare(price1, price2);
                })
                .map(GetClusterListResponse::from)
                .toList();
    }

    private int getComparableCorkagePrice(Restaurant r) {
        CorkageStore cs = r.getCorkageStore();

        return switch (cs.getCorkageType()) {
            case FREE -> 0;
            case MULTIPLE -> cs.getMultiPrices().stream()
                    .mapToInt(MultiCorkage::getPrice)
                    .min().orElse(Integer.MAX_VALUE);
            default -> cs.getCorkagePrice() != null ? cs.getCorkagePrice() : Integer.MAX_VALUE;
        };
    }

}