package konkuk.corkCharge.domain.restaurant.service;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;
import konkuk.corkCharge.domain.bookmark.repository.GroupRestaurantPinProjection;
import konkuk.corkCharge.domain.bookmark.repository.RestaurantBookmarkGroupItemRepository;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.dto.mapper.*;
import konkuk.corkCharge.domain.restaurant.dto.request.*;
import konkuk.corkCharge.domain.restaurant.dto.response.*;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantDistanceProjection;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.api.naverMapsApi.NaverGeocodingClient;
import konkuk.corkCharge.global.api.naverMapsApi.dto.Address;
import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private static final double GANGNAM_LAT = 37.498774;
    private static final double GANGNAM_LON = 127.027829;

    private static final double HONGDAE_LAT = 37.556574;
    private static final double HONGDAE_LON = 126.923405;

    private static final double SEONGSU_LAT = 37.544501;
    private static final double SEONGSU_LON = 127.056079;

    private static final double KONKUK_LAT = 37.540412;
    private static final double KONKUK_LON = 127.069166;

    private static final double ITAEWON_LAT = 37.534489;
    private static final double ITAEWON_LON = 126.993705;

    private static final double YONGSAN_LAT = 37.529764;
    private static final double YONGSAN_LON = 126.964741;

    private static final int RADIUS_METERS_RECOMMEND = 2000;
    private static final int RADIUS_METERS_NEAR_BY = 3000;

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "중국요리",
            "회",
            "이탈리안",
            "초밥",
            "육류,고기"
    );

    private static final int NEW_RESTAURANT_DAYS = 14;

    private final RestaurantRepository restaurantRepository;
    private final NaverGeocodingClient naverGeocodingClient;
    private final CorkageStoreRepository corkageStoreRepository;
    private final RestaurantBookmarkGroupItemRepository groupItemRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    private final ClusterListResponseMapper clusterListResponseMapper;
    private final RestaurantDetailResponseMapper restaurantDetailResponseMapper;
    private final HotRestaurantResponseMapper hotRestaurantResponseMapper;
    private final MapRestaurantResponseMapper mapRestaurantResponseMapper;
    private final RestaurantListResponseMapper restaurantListResponseMapper;
    private final HomeRestaurantCardMapper homeRestaurantCardMapper;

    private final RestaurantSummaryService restaurantSummaryService;
    private final HomeRestaurantResponseMapper homeRestaurantResponseMapper;

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
    public GetClusterListResponse getClusterList(GetClusterListRequest req) {

        if (req.restaurantIds() == null || req.restaurantIds().isEmpty()) {
            return new GetClusterListResponse(0, List.of());
        }

        ClusterListSort sort = (req.sort() == null) ? ClusterListSort.PRICE_ASC : req.sort();

        List<Restaurant> restaurants = restaurantRepository.findAllById(req.restaurantIds());

        List<CorkageStore> corkageStores = corkageStoreRepository.findAllByRestaurantIdIn(req.restaurantIds());

        Map<Long, CorkageStore> corkageMap = corkageStores.stream()
                .collect(Collectors.toMap(
                        cs -> cs.getRestaurant().getRestaurantId(),
                        Function.identity(),
                        (a, b) -> a
                ));

        Map<Long, String[]> imageMap = imageRepository
                .findRestaurantMainImagesByRestaurantIds(req.restaurantIds())
                .stream()
                .collect(Collectors.groupingBy(
                        Image::getTypeId,
                        LinkedHashMap::new,
                        Collectors.mapping(Image::getImageUrl, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toArray(new String[0])
                ));

        // 정렬 방법
        Comparator<Restaurant> comparator = switch (sort) {
            case PRICE_ASC -> Comparator
                    .comparingInt((Restaurant r) -> getComparableCorkagePrice(corkageMap.get(r.getRestaurantId())))
                    .thenComparing(Restaurant::getRestaurantId, Comparator.reverseOrder());

            case REVIEW_COUNT_DESC -> Comparator
                    .comparingInt((Restaurant r) -> r.getReviewCount()).reversed()
                    .thenComparing(Restaurant::getRestaurantId, Comparator.reverseOrder());

            case RATING_DESC -> Comparator
                    .comparingDouble((Restaurant r) -> safeDouble(r.getRating())).reversed()
                    .thenComparing(Comparator.comparingInt((Restaurant r) -> r.getReviewCount()).reversed())
                    .thenComparing(Restaurant::getRestaurantId, Comparator.reverseOrder());
        };

        List<GetClusterListResponse.Item> items = restaurants.stream()
                .sorted(comparator)
                .map(r -> {
                    CorkageStore cs = corkageMap.get(r.getRestaurantId());

                    if (cs == null || cs.getCorkageType() == null) {
                        throw new CustomException(BAD_REQUEST);
                    }

                    return clusterListResponseMapper.toItem(
                            r,
                            cs,
                            imageMap.getOrDefault(r.getRestaurantId(), new String[0])
                    );
                })
                .toList();

        return new GetClusterListResponse(items.size(), items);
    }

    // 가격 숫자화
    private int getComparableCorkagePrice(CorkageStore cs) {
        if (cs == null || cs.getCorkageType() == null) return Integer.MAX_VALUE;

        return switch (cs.getCorkageType()) {
            case FREE -> 0;
            case MULTIPLE -> cs.getMultiPrices().stream()
                    .mapToInt(MultiCorkage::getPrice)
                    .min()
                    .orElse(Integer.MAX_VALUE);
            default -> (cs.getCorkagePrice() != null) ? cs.getCorkagePrice() : Integer.MAX_VALUE;
        };
    }

    private double safeDouble(Double v) {
        return v == null ? 0.0 : v;
    }

    @Transactional(readOnly = true)
    public List<GetHomeRestaurantResponse> getNewRestaurants(UserLocationRequest req) {
        LocalDateTime from = LocalDateTime.now().minusDays(NEW_RESTAURANT_DAYS);

        // 사용자 좌표가 있는 경우
        if (req.hasUserLocation()) {
            List<RestaurantDistanceProjection> rows =
                    restaurantRepository.findNewRestaurantsWithDistance(from, req.lat(), req.lon());

            Map<Long, Double> distanceMap = rows.stream()
                    .collect(Collectors.toMap(
                            RestaurantDistanceProjection::getRestaurantId,
                            RestaurantDistanceProjection::getDistanceKm
                    ));

            return rows.stream()
                    .map(row -> {
                        Long id = row.getRestaurantId();
                        RestaurantSummary summary = restaurantSummaryService.getSummary(id);

                        return homeRestaurantResponseMapper.toResponse(summary, distanceMap.get(id));
                    })
                    .toList();
        }

        // 사용자 좌표가 없는 경우
        List<Restaurant> restaurants = restaurantRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(from);

        return restaurants.stream()
                .map(r -> {
                    Long id = r.getRestaurantId();
                    RestaurantSummary summary = restaurantSummaryService.getSummary(id);

                    return homeRestaurantResponseMapper.toResponse(summary, null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GetHomeRestaurantResponse> getCategoryRestaurants(GetCategoryRestaurantRequest req) {
        if (!ALLOWED_CATEGORIES.contains(req.category())) {
            throw new CustomException(CATEGORY_NOT_FOUND);
        }

        if (req.hasUserLocation()) {
            List<RestaurantDistanceProjection> rows =
                    restaurantRepository.findCategoryRestaurantsWithDistance(
                            req.category(),
                            req.lat(),
                            req.lon()
                    );

            return rows.stream()
                    .map(row -> {
                        Long id = row.getRestaurantId();
                        RestaurantSummary summary = restaurantSummaryService.getSummary(id);
                        return homeRestaurantResponseMapper.toResponse(summary, row.getDistanceKm());
                    })
                    .toList();
        }

        // 좌표 없는 경우
        List<Restaurant> restaurants =
                restaurantRepository.findByHasCorkageTrueAndRawCategoryContainingOrderByBookmarkCountDesc(req.category());

        return restaurants.stream()
                .map(r -> {
                    Long id = r.getRestaurantId();
                    RestaurantSummary summary = restaurantSummaryService.getSummary(id);
                    return homeRestaurantResponseMapper.toResponse(summary, null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GetHomeRestaurantResponse> getNearByRestaurants(UserLocationRequest req) {
        if (req == null || !req.hasUserLocation()) {
            throw new CustomException(LOCATION_REQUIRED);
        }

        List<RestaurantDistanceProjection> rows =
                restaurantRepository.findNearbyRestaurantsWithinRadius(req.lat(), req.lon(), RADIUS_METERS_NEAR_BY);

        return rows.stream()
                .map(row -> {
                    Long id = row.getRestaurantId();
                    RestaurantSummary summary = restaurantSummaryService.getSummary(id);
                    return homeRestaurantResponseMapper.toResponse(summary, row.getDistanceKm());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GetHomeRestaurantResponse> getRecommendRestaurants() {
        List<RestaurantDistanceProjection> rows =
                restaurantRepository.findRecommendRestaurantsWithinRadius(
                        RADIUS_METERS_RECOMMEND,
                        GANGNAM_LAT, GANGNAM_LON,
                        HONGDAE_LAT, HONGDAE_LON,
                        SEONGSU_LAT, SEONGSU_LON,
                        KONKUK_LAT, KONKUK_LON,
                        ITAEWON_LAT, ITAEWON_LON,
                        YONGSAN_LAT, YONGSAN_LON
                );

        return rows.stream()
                .map(row -> {
                    Long id = row.getRestaurantId();
                    RestaurantSummary summary = restaurantSummaryService.getSummary(id);
                    return homeRestaurantResponseMapper.toResponse(summary, row.getDistanceKm());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GetRestaurantTabResponse getHomeRestaurantTab(UserLocationRequest req) {
        final int LIMIT = 5;

        // 가까운 매장 top5
        List<Long> nearbyIds;
        if (req == null || !req.hasUserLocation()) {
            nearbyIds = List.of();
        } else {
            nearbyIds =
                    restaurantRepository.findNearbyRestaurantIdsWithinRadiusLimit(
                            req.lat(), req.lon(), RADIUS_METERS_NEAR_BY, LIMIT
                    );
        }

        // 추천 매장 top5
        List<Long> recommendIds =
                restaurantRepository.findRecommendRestaurantIdsWithinRadiusLimit(
                        RADIUS_METERS_RECOMMEND,
                        GANGNAM_LAT, GANGNAM_LON,
                        HONGDAE_LAT, HONGDAE_LON,
                        SEONGSU_LAT, SEONGSU_LON,
                        KONKUK_LAT, KONKUK_LON,
                        ITAEWON_LAT, ITAEWON_LON,
                        YONGSAN_LAT, YONGSAN_LON,
                        LIMIT
                );

        List<HomeRestaurantCard> nearbyCard = restaurantSummaryService.getSummariesInOrder(nearbyIds).stream()
                .map(homeRestaurantCardMapper::toCard)
                .toList();

        List<HomeRestaurantCard> recommendCard = restaurantSummaryService.getSummariesInOrder(recommendIds).stream()
                .map(homeRestaurantCardMapper::toCard)
                .toList();

        return new GetRestaurantTabResponse(nearbyCard, recommendCard);
    }

    @Transactional(readOnly = true)
    public GetGroupRestaurantPinsResponse getGroupRestaurantPins(Long userId, double latMin, double latMax, double lonMin, double lonMax) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String wkt = toEnvelopeWkt(lonMin, latMin, lonMax, latMax);

        List<GroupRestaurantPinProjection> rows = groupItemRepository.findGroupRestaurantPinsInBounds(userId, wkt);

        // 그룹Id 기준으로 map에 넣음
        Map<Long, GroupAccumulator> map = new LinkedHashMap<>();

        for (GroupRestaurantPinProjection row : rows) {
            GroupAccumulator acc = map.computeIfAbsent(
                    row.getGroupId(),
                    key -> new GroupAccumulator(
                            row.getGroupId(),
                            row.getName(),
                            row.getColor(),
                            row.getVisibility()
                    )
            );

            // 각 그룹에 핀 추가
            acc.pins.add(new GetGroupRestaurantPinsResponse.Pin(
                    row.getRestaurantId(),
                    row.getLatitude(),
                    row.getLongitude()
            ));

            acc.restaurantIds.add(row.getRestaurantId());
        }

        List<GetGroupRestaurantPinsResponse.GroupPins> groups = map.values().stream()
                .map(acc -> new GetGroupRestaurantPinsResponse.GroupPins(
                        acc.groupId,
                        acc.name,
                        acc.color,
                        acc.visibility,
                        acc.restaurantIds.size(),
                        acc.pins
                ))
                .toList();

        return new GetGroupRestaurantPinsResponse(groups.size(), groups);
    }

    private static class GroupAccumulator {
        final Long groupId;
        final String name;
        final String color;
        final BookmarkGroupVisibility visibility;

        final List<GetGroupRestaurantPinsResponse.Pin> pins = new ArrayList<>();
        final Set<Long> restaurantIds = new HashSet<>();

        GroupAccumulator(Long groupId, String name, String color, BookmarkGroupVisibility visibility) {
            this.groupId = groupId;
            this.name = name;
            this.color = color;
            this.visibility = visibility;
        }
    }

}