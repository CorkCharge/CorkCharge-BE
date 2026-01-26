package konkuk.corkCharge.domain.restaurant.dto.response;

public record GetMapRestaurantPinResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        String corkagePrice,
        Double lat,
        Double lon
) {
}
