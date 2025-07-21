package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

import java.util.List;

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
                .filter(image -> image.getCategory() == RESTAURANT)
                .filter(image -> image.getType() == MAIN)
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);

        String menuImageUrl = restaurant.getImages().stream()
                .filter(image -> image.getCategory() == RESTAURANT)
                .filter(image -> image.getType() == MENU)
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);

        CorkageStore corkage = restaurant.getCorkageStore();

        String pairingImageUrl = (corkage != null && corkage.getImages() != null)
                ? corkage.getImages().stream()
                    .filter(image -> image.getCategory() == CORKAGE)
                    .map(Image::getImageUrl)
                    .findFirst()
                    .orElse(null)
                : null;

        String corkagePrice = corkage != null ? corkage.getCorkagePrice() : null;

        List<String> corkageOptions = (corkage != null && corkage.getAdditionalOptions() != null)
                ? List.of(corkage.getAdditionalOptions().split(","))
                : List.of();

        String pairingAlcohol = corkage != null ? corkage.getPairing() : null;

        String pairingDescription = corkagePrice != null ? corkage.getDescription() : null;

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
