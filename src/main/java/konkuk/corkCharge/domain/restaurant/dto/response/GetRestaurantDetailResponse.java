package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.corkageStore.domain.*;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

import java.util.List;
import java.util.stream.Collectors;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.*;
import static konkuk.corkCharge.domain.image.domain.ImageType.*;

public record GetRestaurantDetailResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        String phone,
        Double rating,
        int reviewCount,
        String mainImageUrl,
        String menuImageUrl,
        String corkagePrice,
        List<String> corkageOptions,
        String representMenu,
        String pairingAlcohol,
        String pairingDescription,
        String pairingImageUrl,
        String openingHours,
        List<ReviewResponse> reviews
) {
    public static GetRestaurantDetailResponse from(Restaurant restaurant) {
        String mainImageUrl = restaurant.getImages().stream()
                .filter(image -> image.getCategory() == RESTAURANT && image.getType() == MAIN)
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);

        String menuImageUrl = restaurant.getImages().stream()
                .filter(image -> image.getCategory() == RESTAURANT && image.getType() == MENU)
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);

        CorkageStore corkage = restaurant.getCorkageStore();
        String corkagePrice = null;
        String pairingAlcohol = null;
        String pairingDescription = null;
        String pairingImageUrl = null;
        List<String> corkageOptions = List.of();

        if (corkage != null) {
            pairingImageUrl = corkage.getImages().stream()
                    .filter(image -> image.getCategory() == CORKAGE)
                    .map(Image::getImageUrl)
                    .findFirst()
                    .orElse(null);

            // 콜키지 가격
            if (corkage.getCorkageType() == CorkageType.MULTIPLE) {
                corkagePrice = corkage.getMultiPrices().stream()
                        .map(mp -> mp.getLiquorType() + " 병당: " + mp.getPrice() + "원")
                        .collect(Collectors.joining(", "));
            } else if (corkage.getCorkageType() == CorkageType.FREE) {
                corkagePrice = "콜키지 프리";
            } else {
                corkagePrice = switch (corkage.getCorkageType()) {
                    case PER_BOTTLE -> "병당 " + corkage.getCorkagePrice() + "원";
                    case PER_PERSON -> "인당 " + corkage.getCorkagePrice() + "원";
                    case PER_TABLE -> "테이블당 " + corkage.getCorkagePrice() + "원";
                    default -> null;
                };
            }

            // 옵션
            corkageOptions = corkage.getCorkageOptions().stream()
                    .map(option -> {
                        if (option.getOptionType() == OptionType.ETC && option.getEtcContent() != null) {
                            return option.getEtcContent();
                        } else {
                            return option.getOptionType().getLabel();
                        }
                    })
                    .collect(Collectors.toList());

            pairingAlcohol = corkage.getPairing();
            pairingDescription = corkage.getDescription();
        }

        List<ReviewResponse> reviewResponses = restaurant.getReviews().stream()
                .map(ReviewResponse::from)
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