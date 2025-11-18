package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetClusterListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static konkuk.corkCharge.domain.corkageStore.domain.OptionType.ETC;
import static konkuk.corkCharge.domain.image.domain.ImageCategory.RESTAURANT;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;

@Component
@RequiredArgsConstructor
public class ClusterListResponseMapper {

    private final ImageRepository imageRepository;
    private final CorkageStoreRepository corkageStoreRepository;

    public GetClusterListResponse toClusterListResponse(Restaurant restaurant) {

        CorkageStore corkageStore = corkageStoreRepository
                .findByRestaurant_RestaurantId(restaurant.getRestaurantId())
                .orElseThrow(() -> new IllegalStateException("콜키지 정보가 없습니다. restaurantId=" + restaurant.getRestaurantId()));

        String corkagePrice = switch (corkageStore.getCorkageType()) {
            case FREE -> "FREE";
            case MULTIPLE -> {
                int min = corkageStore.getMultiPrices().stream()
                        .mapToInt(MultiCorkage::getPrice)
                        .min()
                        .orElse(Integer.MAX_VALUE);
                yield min == Integer.MAX_VALUE ? "가격 미정" : "최저 " + min + "원";
            }
            case PER_BOTTLE -> "병당 " + corkageStore.getCorkagePrice() + "원";
            case PER_PERSON -> "인당 " + corkageStore.getCorkagePrice() + "원";
            case PER_TABLE -> "테이블당 " + corkageStore.getCorkagePrice() + "원";
        };

        List<String> corkageOptions;

        int bits = corkageStore.getOptionBits();

        corkageOptions = java.util.Arrays.stream(konkuk.corkCharge.domain.corkageStore.domain.OptionType.values())
                .filter(type -> (bits & (1 << type.ordinal())) != 0)
                .map(type -> {
                    if (type == ETC) {
                        return corkageStore.getEtcContent();
                    } else {
                        return type.getLabel();
                    }
                })
                .filter(opt -> opt != null && !opt.isBlank())
                .toList();

        String imageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(RESTAURANT, restaurant.getRestaurantId(), MAIN)
                .map(Image::getImageUrl)
                .orElse(null);

        return new GetClusterListResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getRating(),
                restaurant.getReviewCount(),
                corkagePrice,
                corkageOptions,
                imageUrl
        );
    }
}