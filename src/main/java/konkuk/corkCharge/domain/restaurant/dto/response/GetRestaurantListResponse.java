package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

public record GetRestaurantListResponse(
        Long restaurantId,
        String name,
        String address,
        int corkagePrice,
        String imageUrl,
        int reviewCount,
        double averageRating,
        int bookmarkCount
) {
    public static GetRestaurantListResponse from(Restaurant restaurant) {
        int corkagePrice = restaurant.getCorkageStore() != null ? restaurant.getCorkageStore().getCorkagePrice() : null;

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
                restaurant.getBookmarkCount()
        );
    }

}
