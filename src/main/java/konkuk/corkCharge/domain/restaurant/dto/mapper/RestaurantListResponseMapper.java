package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.RESTAURANT;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;

@Component
@RequiredArgsConstructor
public class RestaurantListResponseMapper {

    private final CorkageStoreRepository corkageStoreRepository;
    private final ImageRepository imageRepository;

    public GetRestaurantListResponse toResponse(Restaurant restaurant) {
        // 콜키지 정보
        CorkageStore corkage = corkageStoreRepository
                .findByRestaurant_RestaurantId(restaurant.getRestaurantId())
                .orElse(null);

        String corkagePrice = buildCorkagePrice(corkage);

        // 메인 이미지
        String imageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(RESTAURANT, restaurant.getRestaurantId(), MAIN)
                .map(Image::getImageUrl)
                .orElse(null);

        return new GetRestaurantListResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                corkagePrice,
                imageUrl,
                restaurant.getReviewCount(),
                restaurant.getRating(),
                restaurant.getBookmarkCount(),
                restaurant.getOpeningHours()
        );
    }

    private String buildCorkagePrice(CorkageStore corkage) {
        if (corkage == null || corkage.getCorkageType() == null) {
            return null;
        }

        CorkageType type = corkage.getCorkageType();

        if (type == CorkageType.MULTIPLE) {
            return corkage.getMultiPrices().stream()
                    .map(mp -> mp.getLiquorType() + " 병당: " + mp.getPrice() + "원")
                    .collect(Collectors.joining(", "));
        }

        if (type == CorkageType.FREE) {
            return "콜키지 프리";
        }

        return switch (type) {
            case PER_BOTTLE -> "병당 " + corkage.getCorkagePrice() + "원";
            case PER_PERSON -> "인당 " + corkage.getCorkagePrice() + "원";
            case PER_TABLE -> "테이블당 " + corkage.getCorkagePrice() + "원";
            default -> null;
        };
    }
}
