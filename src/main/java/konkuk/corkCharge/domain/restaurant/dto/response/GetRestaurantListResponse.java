package konkuk.corkCharge.domain.restaurant.dto.response;

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
}
