package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
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

    public GetRestaurantListResponse toResponse(RestaurantSummary s) {
        // 콜키지 정보
        CorkageStore corkage = corkageStoreRepository
                .findByRestaurant_RestaurantId(s.getRestaurantId())
                .orElse(null);

        // 메인 이미지
        String imageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(RESTAURANT, s.getRestaurantId(), MAIN)
                .map(Image::getImageUrl)
                .orElse(null);

        return new GetRestaurantListResponse(
                s.getRestaurantId(),
                s.getName(),
                s.getAddress(),
                s.getCorkagePrice(),
                s.getMainImageUrl(),
                s.getReviewCount(),
                s.getAvgRating(),
                s.getBookmarkCount(),
                s.getOpeningHours()
        );
    }
}
