package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantDetailResponse;
import konkuk.corkCharge.domain.restaurant.dto.response.ReviewResponse;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import konkuk.corkCharge.domain.image.domain.Image;

import java.util.List;
import java.util.stream.Collectors;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.CORKAGE;
import static konkuk.corkCharge.domain.image.domain.ImageCategory.RESTAURANT;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;
import static konkuk.corkCharge.domain.image.domain.ImageType.MENU;

@Component
@RequiredArgsConstructor
public class RestaurantDetailResponseMapper {

    private final ImageRepository imageRepository;
    private final CorkageStoreRepository corkageStoreRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewResponseMapper reviewResponseMapper;

    public GetRestaurantDetailResponse toResponse(Restaurant restaurant) {

        // 메인 / 메뉴 이미지
        String mainImageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(RESTAURANT, restaurant.getRestaurantId(), MAIN)
                .map(Image::getImageUrl)
                .orElse(null);

        String menuImageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(RESTAURANT, restaurant.getRestaurantId(), MENU)
                .map(Image::getImageUrl)
                .orElse(null);

        // 콜키지 정보
        CorkageStore corkage = corkageStoreRepository
                .findByRestaurant_RestaurantId(restaurant.getRestaurantId())
                .orElse(null);

        String corkagePrice = null;
        String pairingAlcohol = null;
        String pairingDescription = null;
        String pairingImageUrl = null;
        List<String> corkageOptions = List.of();

        // 콜키지
        if (corkage != null) {
            pairingImageUrl = imageRepository
                    .findFirstByCategoryAndTypeIdAndType(CORKAGE, corkage.getCorkageStoreId(), MAIN)
                    .map(Image::getImageUrl)
                    .orElse(null);

            CorkageType type = corkage.getCorkageType();
            if (type == CorkageType.MULTIPLE) {
                corkagePrice = corkage.getMultiPrices().stream()
                        .map(mp -> mp.getLiquorType() + " 병당: " + mp.getPrice() + "원")
                        .collect(Collectors.joining(", "));
            } else if (type == CorkageType.FREE) {
                corkagePrice = "콜키지 프리";
            } else {
                corkagePrice = switch (type) {
                    case PER_BOTTLE -> "병당 " + corkage.getCorkagePrice() + "원";
                    case PER_PERSON -> "인당 " + corkage.getCorkagePrice() + "원";
                    case PER_TABLE -> "테이블당 " + corkage.getCorkagePrice() + "원";
                    default -> null;
                };
            }

            corkageOptions = corkage.getCorkageOptions().stream()
                    .map(option -> {
                        if (option.getOptionType() == OptionType.ETC && option.getEtcContent() != null) {
                            return option.getEtcContent();
                        } else {
                            return option.getOptionType().getLabel();
                        }
                    })
                    .toList();

            pairingAlcohol = corkage.getPairing();
            pairingDescription = corkage.getDescription();
        }

        // 리뷰
        List<Review> reviews =
                reviewRepository.findByRestaurant_RestaurantId(restaurant.getRestaurantId());

        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(reviewResponseMapper::toResponse)
                .toList();

        return new GetRestaurantDetailResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getRating(),
                restaurant.getReviewCount(),
                mainImageUrl,
                menuImageUrl,
                corkagePrice,
                corkageOptions,
                restaurant.getRepresentMenu(),
                pairingAlcohol,
                pairingDescription,
                pairingImageUrl,
                restaurant.getOpeningHours(),
                reviewResponses
        );
    }
}
