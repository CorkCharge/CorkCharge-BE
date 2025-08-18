package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

import java.util.stream.Collectors;

public record GetRestaurantListResponse(
        Long restaurantId,
        String name,
        String address,
        String corkagePrice,
        String imageUrl,
        int reviewCount,
        double averageRating,
        int bookmarkCount,
        String openingHours
) {
    public static GetRestaurantListResponse from(Restaurant restaurant) {
        CorkageStore corkage = restaurant.getCorkageStore();
        String corkagePrice = null;

        if (corkage != null) {
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
        }

        String imageUrl = restaurant.getImages().stream()
                .filter(image -> image.getType() == ImageType.MAIN)
                .map(Image::getImageUrl)
                .findFirst()
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

}
