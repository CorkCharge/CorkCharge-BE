package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

public record GetRestaurantMapResponse(
        Long restaurantId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String corkagePrice
) {
    public static GetRestaurantMapResponse of(Restaurant restaurant, String corkagePrice) {
        return new GetRestaurantMapResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                corkagePrice
        );
    }
}
