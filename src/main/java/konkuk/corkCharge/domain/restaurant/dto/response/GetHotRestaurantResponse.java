package konkuk.corkCharge.domain.restaurant.dto.response;

public record GetHotRestaurantResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        int bookmarkCount,
        String openingHours,
        String imageUrl
) {
}
