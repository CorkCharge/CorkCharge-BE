package konkuk.corkCharge.domain.restaurant.dto.response;

public record GetHomeRestaurantResponse(
        Long restaurantId,
        String restaurantName,
        int bookmarkCount,
        String imageUrl
) {
}
