package konkuk.corkCharge.domain.bookmark.dto.response;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

import java.time.LocalDateTime;
import java.util.List;

public record GetSavedRestaurantResponse(
        Long bookmarkId,
        Long restaurantId,
        String name,
        String address,
        String thumbnailUrl,
        double rating,
        int reviewCount,
        int bookmarkCount,
        boolean hasCorkage,
        CorkageType corkageType,
        Integer corkagePrice,
        LocalDateTime createdAt
) {
    public static GetSavedRestaurantResponse from(Bookmark bookmark,
                                                  Restaurant restaurant,
                                                  List<Image> images,
                                                  CorkageStore corkageStore){
        String thumbnail = images.stream()
                .map(Image::getImageUrl)
                .findFirst()
                .orElse("");

        CorkageType type = corkageStore != null ? corkageStore.getCorkageType() : null;
        Integer price = corkageStore != null ? corkageStore.getCorkagePrice() : null;

        return new GetSavedRestaurantResponse(
                bookmark.getId(),
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                thumbnail,
                restaurant.getRating() != null ? restaurant.getRating() : 0.0,
                restaurant.getReviewCount(),
                restaurant.getBookmarkCount(),
                restaurant.isHasCorkage(),
                type,
                price,
                bookmark.getCreatedAt()
        );
    }
}
