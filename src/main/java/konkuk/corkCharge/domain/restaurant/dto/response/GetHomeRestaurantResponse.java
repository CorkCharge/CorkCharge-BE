package konkuk.corkCharge.domain.restaurant.dto.response;

import java.util.List;

public record GetHomeRestaurantResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        Double rating,
        Integer reviewCount,
        String corkagePrice,
        List<String> corkageOptions,
        Double distance,          // km기준, lat/lon 없으면 null
        String mainImageUrls,
        String openingHours
) {
}
