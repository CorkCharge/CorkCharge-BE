package konkuk.corkCharge.domain.restaurant.service;

import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.dto.mapper.*;
import konkuk.corkCharge.domain.restaurant.dto.request.GetFilterRequest;
import konkuk.corkCharge.domain.restaurant.dto.request.GetNewRestaurantRequest;
import konkuk.corkCharge.domain.restaurant.dto.response.*;
import konkuk.corkCharge.domain.restaurant.repository.NewRestaurantDistanceProjection;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.api.naverMapsApi.NaverGeocodingClient;
import konkuk.corkCharge.global.api.naverMapsApi.dto.Address;
import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.RESTAURANT;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private static final int NEW_RESTAURANT_DAYS = 14;

    private final RestaurantRepository restaurantRepository;
    private final NaverGeocodingClient naverGeocodingClient;
    private final ImageRepository imageRepository;
    private final CorkageStoreRepository corkageStoreRepository;

    private final ClusterListResponseMapper clusterListResponseMapper;
    private final RestaurantDetailResponseMapper restaurantDetailResponseMapper;
    private final HotRestaurantResponseMapper hotRestaurantResponseMapper;
    private final MapRestaurantResponseMapper mapRestaurantResponseMapper;
    private final RestaurantListResponseMapper restaurantListResponseMapper;

    private final RestaurantSummaryService restaurantSummaryService;
    private final NewRestaurantResponseMapper newRestaurantResponseMapper;

    @Transactional(readOnly = true)
    public List<GetRestaurantListResponse> getCorkageRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByHasCorkageTrue();

        if (restaurants.isEmpty()) {
            throw new CustomException(CORKAGE_RESTAURANT_NOT_FOUND);
        }

        return restaurants.stream()
                .map(r -> restaurantSummaryService.getSummary(r.getRestaurantId()))
                .map(restaurantListResponseMapper::toResponse)
                .toList();
    }

    public GetRestaurantDetailResponse getRestaurantDetail(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        RestaurantSummary summary = restaurantSummaryService.getSummary(restaurantId);

        return restaurantDetailResponseMapper.toResponse(summary);
    }

    @Transactional
    public List<GetSearchRestaurantResponse> searchRestaurants(String keyword) {
        List<Restaurant> matchedRestaurants = restaurantRepository.findByNameContaining(keyword);

        return matchedRestaurants.stream()
                .map(GetSearchRestaurantResponse::from)
                .toList();
    }

//    @Transactional
//    public List<GetHotRestaurantResponse> getHotRestaurants() {
//        List<Restaurant> hotRestaurants = restaurantRepository.findByHasCorkageFalseAndBookmarkCountGreaterThanEqual(5);
//
//        return hotRestaurants.stream()
//                .map(hotRestaurantResponseMapper::toResponse)
//                .toList();
//    }

    @Transactional
    public List<?> filterRestaurants(GetFilterRequest request) {
        List<Restaurant> matchedRestaurants = filterByAddress(request.sido(), request.sigungu(), request.dongList());

        return switch (request.type()) {
            case "hot" -> matchedRestaurants.stream()
                    .filter(r -> r.getBookmarkCount() >= 5)
                    .map(r -> restaurantSummaryService.getSummary(r.getRestaurantId()))
                    .map(hotRestaurantResponseMapper::toResponse)
                    .toList();

            case "map" -> matchedRestaurants.stream()
                    .filter(Restaurant::isHasCorkage)
                    .map(r -> restaurantSummaryService.getSummary(r.getRestaurantId()))
                    .map(s -> new GetSearchRestaurantResponse(
                            s.getRestaurantId(),
                            s.getName(),
                            s.getAddress()
                    ))
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
        updateMissingLocations();
        // DB에서 바로 공간 인덱스 기반으로 범위 내 매장 검색
        String wkt = toEnvelopeWkt(lonMin, latMin, lonMax, latMax);
        List<Restaurant> filtered = restaurantRepository.findCorkageRestaurantsInBounds(wkt);

        if (filtered.isEmpty()) {
            throw new CustomException(CORKAGE_RESTAURANT_NOT_FOUND);
        }

        return switch (level) {
            case "restaurant" -> filtered.stream()
                    .map(mapRestaurantResponseMapper::toResponse)
                    .toList();

            case "dong", "sigungu", "sido" -> filtered.stream()
                    .map(GetMapClusterResponse::from)
                    .toList();

            default -> throw new CustomException(BAD_REQUEST);
        };
    }

    private String toEnvelopeWkt(double lonMin, double latMin, double lonMax, double latMax) {
        return String.format(
                "POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",
                latMin, lonMin,
                latMin, lonMax,
                latMax, lonMax,
                latMax, lonMin,
                latMin, lonMin
        );
    }

    @Transactional
    public void updateMissingLocations() {
        List<Restaurant> targets = restaurantRepository.findRestaurantsWithoutValidCoordinates();
        if (targets.isEmpty()) return;

        targets.forEach(restaurant -> {
            NaverMapsResponse response = naverGeocodingClient.getCoordinatesByAddress(restaurant.getAddress());
            if (!response.addresses().isEmpty()) {
                Address address = response.addresses().get(0);
                double lat = Double.parseDouble(address.latitude());
                double lon = Double.parseDouble(address.longitude());
                restaurant.updateCoordinates(lat, lon);
                // location은 DB가 자동 갱신함
            }
        });
    }

    @Transactional(readOnly = true)
    public List<GetClusterListResponse> getClusterList(List<Long> restaurantIds) {
        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);

        return restaurants.stream()
                .sorted(Comparator.comparingInt(this::getComparableCorkagePrice))
                .map(clusterListResponseMapper::toClusterListResponse)
                .toList();
    }

    private int getComparableCorkagePrice(Restaurant r) {
        CorkageStore cs = corkageStoreRepository
                .findByRestaurant_RestaurantId(r.getRestaurantId())
                .orElse(null);

        // 콜키지 정보가 없으면 가장 큰 값으로 취급해서 “비싼 곳”처럼 뒤로 밀기
        if (cs == null || cs.getCorkageType() == null) {
            return Integer.MAX_VALUE;
        }

        return switch (cs.getCorkageType()) {
            case FREE -> 0;
            case MULTIPLE -> cs.getMultiPrices().stream()
                    .mapToInt(MultiCorkage::getPrice)
                    .min()
                    .orElse(Integer.MAX_VALUE);
            default -> cs.getCorkagePrice() != null
                    ? cs.getCorkagePrice()
                    : Integer.MAX_VALUE;
        };
    }

    @Transactional(readOnly = true)
    public GetHomeRestaurantResponse getHomeRestaurant() {
        Restaurant r = restaurantRepository
                .findFirstByHasCorkageFalseOrderByBookmarkCountDesc()
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        // 1순위: 레스토랑 MAIN 이미지
        String imageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(RESTAURANT, r.getRestaurantId(), MAIN)
                // 2순위(없으면): 아무 레스토랑 이미지 한 장
                .or(() -> imageRepository.findFirstByCategoryAndTypeIdOrderByCreatedAtAsc(RESTAURANT, r.getRestaurantId()))
                .map(Image::getImageUrl)
                .orElse(null);

        return new GetHomeRestaurantResponse(
                r.getRestaurantId(),
                r.getName(),
                r.getBookmarkCount() == null ? 0 : r.getBookmarkCount(),
                imageUrl
        );
    }

    @Transactional(readOnly = true)
    public List<GetNewRestaurantResponse> getNewRestaurants(GetNewRestaurantRequest request) {
        LocalDateTime from = LocalDateTime.now().minusDays(NEW_RESTAURANT_DAYS);

        // 사용자 좌표가 있는 경우
        if (request.hasUserLocation()) {
            List<NewRestaurantDistanceProjection> rows =
                    restaurantRepository.findNewRestaurantsWithDistance(from, request.lat(), request.lon());

            Map<Long, Double> distanceMap = rows.stream()
                    .collect(Collectors.toMap(
                            NewRestaurantDistanceProjection::getRestaurantId,
                            NewRestaurantDistanceProjection::getDistanceKm
                    ));

            return rows.stream()
                    .map(row -> {
                        Long id = row.getRestaurantId();
                        RestaurantSummary summary = restaurantSummaryService.getSummary(id);

                        return newRestaurantResponseMapper.toResponse(summary, distanceMap.get(id));
                    })
                    .toList();
        }

        // 사용자 좌표가 없는 경우
        List<Restaurant> restaurants = restaurantRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(from);

        return restaurants.stream()
                .map(r -> {
                    Long id = r.getRestaurantId();
                    RestaurantSummary summary = restaurantSummaryService.getSummary(id);

                    return newRestaurantResponseMapper.toResponse(summary, null);
                })
                .toList();
    }

}