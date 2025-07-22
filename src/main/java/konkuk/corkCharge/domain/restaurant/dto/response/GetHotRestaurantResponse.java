package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.*;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;

public record GetHotRestaurantResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        int bookmarkCount,
        String openingHours,
        String imageUrl
) {
    public static GetHotRestaurantResponse from(Restaurant restaurant) {
        String imageUrl = restaurant.getImages().stream()
                .filter(img -> img.getCategory() == RESTAURANT && img.getType() == MAIN)
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);

        return new GetHotRestaurantResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getBookmarkCount(),
                restaurant.getOpeningHours(),
                imageUrl
        );
    }
}
