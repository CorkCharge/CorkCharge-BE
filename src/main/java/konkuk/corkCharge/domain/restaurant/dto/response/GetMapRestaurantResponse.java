package konkuk.corkCharge.domain.restaurant.dto.response;

public record GetMapRestaurantResponse(
        Long restaurantId,
        Double latitude,
        Double longitude,
        String price
) {
}
