package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

public record GetMapClusterResponse(
        Long restaurantId,
        Double latitude,
        Double longitude,
        String address
) {
    public static GetMapClusterResponse from(Restaurant r) {
        return new GetMapClusterResponse(
                r.getRestaurantId(),
                r.getLatitude(),
                r.getLongitude(),
                r.getAddress()
        );
    }
}
