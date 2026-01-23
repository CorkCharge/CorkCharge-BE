package konkuk.corkCharge.domain.helpRequest.dto.response;

import java.util.List;

public record GetHelpRequestRestaurantsResponse(
        List<RestaurantInfoSummary> restaurants
) {
    public record RestaurantInfoSummary(
            Long restaurantId,
            String name,
            String address,
            Integer requestCount,
            String openingHoursText,
            String imageUrl
    ) {}
}