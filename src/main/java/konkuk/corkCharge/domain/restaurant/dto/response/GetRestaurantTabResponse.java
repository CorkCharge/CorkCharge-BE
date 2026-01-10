package konkuk.corkCharge.domain.restaurant.dto.response;

import java.util.List;

public record GetRestaurantTabResponse(
        List<HomeRestaurantCard> nearbyCard,
        List<HomeRestaurantCard> recommandCard
) {
}
