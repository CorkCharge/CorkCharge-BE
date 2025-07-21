package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

public record GetSearchRestaurantResponse(
        Long restaurantId,
        String name,
        String address
) {
    public static GetSearchRestaurantResponse from(Restaurant restaurant) {
        return new GetSearchRestaurantResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress()
        );
    }
}
