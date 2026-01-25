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

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "restaurantSummary", key = "#p0")
    public RestaurantSummary getSummary(Long restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        // RESTAURANT images
        List<Image> mainImageEntities = imageRepository
                .findAllByCategoryAndTypeIdAndType(ImageCategory.RESTAURANT, restaurantId, ImageType.MAIN)
                .stream()
                .sorted(Comparator.comparing(Image::getCreatedAt))
                .toList();

        List<Image> menuImageEntities = imageRepository
                .findAllByCategoryAndTypeIdAndType(ImageCategory.RESTAURANT, restaurantId, ImageType.MENU)
                .stream()
                .sorted(Comparator.comparing(Image::getCreatedAt))
                .toList();

        List<String> mainImages = mainImageEntities.stream().map(Image::getImageUrl).toList();
        List<String> menuImages = menuImageEntities.stream().map(Image::getImageUrl).toList();

        String mainImage = firstOrNull(mainImages);
        String menuImage = firstOrNull(menuImages);

        CorkageStore corkage = corkageStoreRepository
                .findByRestaurant_RestaurantId(restaurantId)
                .orElse(null);

        List<String> pairingImages = List.of();
        String pairingImage = null;

        String corkagePrice = null;
        Integer optionBits = null;
        String optionEtcContent = null;
        String pairingAlcohol = null;
        String pairingDescription = null;
        CorkageType corkageType = null;

        if (corkage != null) {
            List<Image> pairingImageEntities = imageRepository
                    .findAllByCategoryAndTypeId(ImageCategory.CORKAGE, corkage.getCorkageStoreId())
                    .stream()
                    .sorted(Comparator.comparing(Image::getCreatedAt))
                    .toList();

            pairingImages = pairingImageEntities.stream().map(Image::getImageUrl).toList();
            pairingImage = firstOrNull(pairingImages);

            corkageType = corkage.getCorkageType();
            corkagePrice = buildCorkagePrice(corkage);

            optionBits = corkage.getOptionBits();
            optionEtcContent = corkage.getEtcContent();

            pairingAlcohol = corkage.getPairing();
            pairingDescription = corkage.getDescription();
        }

        return RestaurantSummary.builder()
                .restaurantId(restaurantId)
                .name(restaurant.getName())

                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .representMenu(restaurant.getRepresentMenu())
                .openingHours(restaurant.getOpeningHours())

                // 이미지 (리스트)
                .mainImages(mainImages)
                .menuImages(menuImages)
                .pairingImages(pairingImages)

                // 이미지 (1장)
                .mainImageUrl(mainImage)
                .menuImageUrl(menuImage)
                .pairingImageUrl(pairingImage)

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

        // 3) RESTAURANT 이미지 bulk
        List<Image> restaurantImages = imageRepository.findRestaurantImagesByRestaurantIds(restaurantIds);

        List<Image> sortedRestaurantImages = restaurantImages.stream()
                .sorted(Comparator.comparing(Image::getCreatedAt))
                .toList();

        Map<Long, List<String>> mainImagesMap = sortedRestaurantImages.stream()
                .filter(img -> img.getType() == ImageType.MAIN)
                .collect(Collectors.groupingBy(
                        Image::getTypeId,
                        LinkedHashMap::new,
                        Collectors.mapping(Image::getImageUrl, Collectors.toList())
                ));

        Map<Long, List<String>> menuImagesMap = sortedRestaurantImages.stream()
                .filter(img -> img.getType() == ImageType.MENU)
                .collect(Collectors.groupingBy(
                        Image::getTypeId,
                        LinkedHashMap::new,
                        Collectors.mapping(Image::getImageUrl, Collectors.toList())
                ));

        // 4) CORKAGE 이미지 bulk
        List<Long> corkageIds = corkageStores.stream()
                .map(CorkageStore::getCorkageStoreId)
                .toList();

        Map<Long, List<String>> pairingImagesMap = new HashMap<>();
        if (!corkageIds.isEmpty()) {
            List<Image> corkageImages = imageRepository.findCorkageImagesByCorkageIds(corkageIds);

            List<Image> sortedCorkageImages = corkageImages.stream()
                    .sorted(Comparator.comparing(Image::getCreatedAt))
                    .toList();

            pairingImagesMap = sortedCorkageImages.stream()
                    .collect(Collectors.groupingBy(
                            Image::getTypeId, // corkage_store_id
                            LinkedHashMap::new,
                            Collectors.mapping(Image::getImageUrl, Collectors.toList())
                    ));
        }

        // 5) 순서 유지
        List<RestaurantSummary> result = new ArrayList<>(restaurantIds.size());

        for (Long restaurantId : restaurantIds) {
            Restaurant restaurant = restaurantMap.get(restaurantId);
            if (restaurant == null) continue;

            CorkageStore corkage = corkageMap.get(restaurantId);

            List<String> mainImages = mainImagesMap.getOrDefault(restaurantId, List.of());
            List<String> menuImages = menuImagesMap.getOrDefault(restaurantId, List.of());

            String mainImage = firstOrNull(mainImages);
            String menuImage = firstOrNull(menuImages);

            List<String> pairingImages = List.of();
            String pairingImage = null;

            if (corkage != null) {
                pairingImages = pairingImagesMap.getOrDefault(corkage.getCorkageStoreId(), List.of());
                pairingImage = firstOrNull(pairingImages);
            }

            result.add(buildSummary(
                    restaurant,
                    corkage,
                    mainImages, menuImages, pairingImages,
                    mainImage, menuImage, pairingImage
            ));
        }

        return result;
    }

    private RestaurantSummary buildSummary(
            Restaurant restaurant,
            CorkageStore corkage,
            List<String> mainImages,
            List<String> menuImages,
            List<String> pairingImages,
            String mainImage,
            String menuImage,
            String pairingImage
    ) {
        String corkagePrice = null;
        Integer optionBits = null;
        String optionEtcContent = null;
        String pairingAlcohol = null;
        String pairingDescription = null;
        CorkageType corkageType = null;

        if (corkage != null) {
            corkageType = corkage.getCorkageType();
            corkagePrice = buildCorkagePrice(corkage);

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

                // 리스트
                .mainImages(mainImages == null ? List.of() : mainImages)
                .menuImages(menuImages == null ? List.of() : menuImages)
                .pairingImages(pairingImages == null ? List.of() : pairingImages)

                // 대표 1장
                .mainImageUrl(mainImage)
                .menuImageUrl(menuImage)
                .pairingImageUrl(pairingImage)

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

    private String buildCorkagePrice(CorkageStore corkage) {
        if (corkage == null || corkage.getCorkageType() == null) return null;

        CorkageType type = corkage.getCorkageType();

        if (type == CorkageType.MULTIPLE) {
            return corkage.getMultiPrices().stream()
                    .map(mp -> mp.getLiquorType() + " 병당: " + mp.getPrice() + "원")
                    .collect(Collectors.joining(", "));
        }

        if (type == CorkageType.FREE) return "콜키지 프리";

        Integer price = corkage.getCorkagePrice();
        return switch (type) {
            case PER_BOTTLE -> "병당 " + price + "원";
            case PER_PERSON -> "인당 " + price + "원";
            case PER_TABLE  -> "테이블당 " + price + "원";
            default -> null;
        };
    }

    private String firstOrNull(List<String> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    /**
     * 쓰기 작업 후 Summary 캐시 제거용
     */
    @CacheEvict(cacheNames = "restaurantSummary", key = "#p0")
    public void evictSummary(Long restaurantId) {
        // 캐시만 날리면 되므로 바디 없음
    }

}
