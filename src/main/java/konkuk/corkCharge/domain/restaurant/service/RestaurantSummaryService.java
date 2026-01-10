package konkuk.corkCharge.domain.restaurant.service;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class RestaurantSummaryService {
    private final RestaurantRepository restaurantRepository;
    private final CorkageStoreRepository corkageStoreRepository;
    private final ImageRepository imageRepository;


    /**
     * 레스토랑 요약 정보를 Redis에 캐싱.
     * cacheNames = "restaurantSummary"
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "restaurantSummary", key = "#p0")
    public RestaurantSummary getSummary(Long restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        // 1. 이미지 (메인/메뉴)
        String mainImageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(ImageCategory.RESTAURANT, restaurantId, ImageType.MAIN)
                .map(Image::getImageUrl)
                .orElse(null);

        String menuImageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(ImageCategory.RESTAURANT, restaurantId, ImageType.MENU)
                .map(Image::getImageUrl)
                .orElse(null);

        // 2. 콜키지 정보
        CorkageStore corkage = corkageStoreRepository
                .findByRestaurant_RestaurantId(restaurantId)
                .orElse(null);

        String corkagePrice = null;
        Integer optionBits = null;
        String optionEtcContent = null;
        String pairingAlcohol = null;
        String pairingDescription = null;
        String pairingImageUrl = null;
        CorkageType corkageType = null;

        if (corkage != null) {

            // pairing 이미지
            pairingImageUrl = imageRepository
                    .findFirstByCategoryAndTypeId(ImageCategory.CORKAGE, corkage.getCorkageStoreId())
                    .map(Image::getImageUrl)
                    .orElse(null);

            corkageType = corkage.getCorkageType();

            // 콜키지 가격 문자열 생성
            if (corkageType == CorkageType.MULTIPLE) {
                corkagePrice = corkage.getMultiPrices().stream()
                        .map(mp -> mp.getLiquorType() + " 병당: " + mp.getPrice() + "원")
                        .collect(Collectors.joining(", "));
            } else if (corkageType == CorkageType.FREE) {
                corkagePrice = "콜키지 프리";
            } else {
                corkagePrice = switch (corkageType) {
                    case PER_BOTTLE -> "병당 " + corkage.getCorkagePrice() + "원";
                    case PER_PERSON -> "인당 " + corkage.getCorkagePrice() + "원";
                    case PER_TABLE -> "테이블당 " + corkage.getCorkagePrice() + "원";
                    default -> null;
                };
            }

            // 옵션 비트 / ETC 내용
            optionBits = corkage.getOptionBits();
            optionEtcContent = corkage.getEtcContent();    // ETC 텍스트 (있다면)

            // ---- 페어링 ----
            pairingAlcohol = corkage.getPairing();
            pairingDescription = corkage.getDescription();
        }

        // Summary 생성
        return RestaurantSummary.builder()
                .restaurantId(restaurantId)
                .name(restaurant.getName())

                // 기본 정보
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .representMenu(restaurant.getRepresentMenu())
                .openingHours(restaurant.getOpeningHours())

                // 이미지
                .mainImageUrl(mainImageUrl)
                .menuImageUrl(menuImageUrl)
                .pairingImageUrl(pairingImageUrl)

                // 리뷰 / 북마크
                .reviewCount(restaurant.getReviewCount())
                .avgRating(restaurant.getRating())
                .bookmarkCount(restaurant.getBookmarkCount())

                // 콜키지
                .hasCorkage(restaurant.isHasCorkage())
                .corkageType(corkageType)
                .corkagePrice(corkagePrice)
                .optionBits(optionBits)
                .optionEtcContent(optionEtcContent)

                // 페어링
                .pairingAlcohol(pairingAlcohol)
                .pairingDescription(pairingDescription)

                // 위치
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())

                .build();
    }

    @Transactional(readOnly = true)
    public List<RestaurantSummary> getSummariesInOrder(List<Long> restaurantIds) {
        if (restaurantIds == null || restaurantIds.isEmpty()) return List.of();

        // 1) Restaurant bulk
        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);
        Map<Long, Restaurant> restaurantMap = restaurants.stream()
                .collect(Collectors.toMap(Restaurant::getRestaurantId, r -> r));

        // 2) CorkageStore bulk
        List<CorkageStore> corkageStores = corkageStoreRepository.findAllByRestaurantIdIn(restaurantIds);
        Map<Long, CorkageStore> corkageMap = corkageStores.stream()
                .collect(Collectors.toMap(cs -> cs.getRestaurant().getRestaurantId(), cs -> cs));

        // 3) RESTAURANT 이미지
        List<Image> restaurantImages = imageRepository.findRestaurantImagesByRestaurantIds(restaurantIds);

        Map<Long, String> mainImageMap = new HashMap<>();
        Map<Long, String> menuImageMap = new HashMap<>();
        for (Image img : restaurantImages) {
            if (img.getType() == ImageType.MAIN) mainImageMap.putIfAbsent(img.getTypeId(), img.getImageUrl());
            if (img.getType() == ImageType.MENU) menuImageMap.putIfAbsent(img.getTypeId(), img.getImageUrl());
        }

        // 4) CORKAGE 이미지
        List<Long> corkageIds = corkageStores.stream()
                .map(CorkageStore::getCorkageStoreId)
                .toList();

        Map<Long, String> corkageImageMap = new HashMap<>();
        if (!corkageIds.isEmpty()) {
            List<Image> corkageImages = imageRepository.findCorkageImagesByCorkageIds(corkageIds);
            for (Image img : corkageImages) {
                // typeId = corkageStoreId
                corkageImageMap.putIfAbsent(img.getTypeId(), img.getImageUrl());
            }
        }

        // 5) 순서 유지
        List<RestaurantSummary> result = new ArrayList<>(restaurantIds.size());
        for (Long id : restaurantIds) {
            Restaurant restaurant = restaurantMap.get(id);
            if (restaurant == null) continue;

            CorkageStore corkage = corkageMap.get(id);

            String main = mainImageMap.get(id);
            String menu = menuImageMap.get(id);
            String pairing = (corkage == null) ? null : corkageImageMap.get(corkage.getCorkageStoreId());

            result.add(buildSummary(restaurant, corkage, main, menu, pairing));
        }

        return result;
    }

    private RestaurantSummary buildSummary(
            Restaurant restaurant,
            CorkageStore corkage,
            String mainImageUrl,
            String menuImageUrl,
            String pairingImageUrl
    ) {
        String corkagePrice = null;
        Integer optionBits = null;
        String optionEtcContent = null;
        String pairingAlcohol = null;
        String pairingDescription = null;
        CorkageType corkageType = null;

        if (corkage != null) {
            corkageType = corkage.getCorkageType();

            if (corkageType == CorkageType.MULTIPLE) {
                corkagePrice = corkage.getMultiPrices().stream()
                        .map(mp -> mp.getLiquorType() + " 병당: " + mp.getPrice() + "원")
                        .collect(Collectors.joining(", "));
            } else if (corkageType == CorkageType.FREE) {
                corkagePrice = "콜키지 프리";
            } else {
                corkagePrice = switch (corkageType) {
                    case PER_BOTTLE -> "병당 " + corkage.getCorkagePrice() + "원";
                    case PER_PERSON -> "인당 " + corkage.getCorkagePrice() + "원";
                    case PER_TABLE -> "테이블당 " + corkage.getCorkagePrice() + "원";
                    default -> null;
                };
            }

            optionBits = corkage.getOptionBits();
            optionEtcContent = corkage.getEtcContent();
            pairingAlcohol = corkage.getPairing();
            pairingDescription = corkage.getDescription();
        }

        return RestaurantSummary.builder()
                .restaurantId(restaurant.getRestaurantId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .representMenu(restaurant.getRepresentMenu())
                .openingHours(restaurant.getOpeningHours())
                .mainImageUrl(mainImageUrl)
                .menuImageUrl(menuImageUrl)
                .pairingImageUrl(pairingImageUrl)
                .reviewCount(restaurant.getReviewCount())
                .avgRating(restaurant.getRating())
                .bookmarkCount(restaurant.getBookmarkCount())
                .hasCorkage(restaurant.isHasCorkage())
                .corkageType(corkageType)
                .corkagePrice(corkagePrice)
                .optionBits(optionBits)
                .optionEtcContent(optionEtcContent)
                .pairingAlcohol(pairingAlcohol)
                .pairingDescription(pairingDescription)
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .build();
    }

    /**
     * 쓰기 작업 후 Summary 캐시 제거용
     */
    @CacheEvict(cacheNames = "restaurantSummary", key = "#restaurantId")
    public void evictSummary(Long restaurantId) {
        // 캐시만 날리면 되므로 바디 없음
    }

}
