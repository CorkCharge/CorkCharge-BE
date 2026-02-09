package konkuk.corkCharge.domain.restaurant.dto.response;

import java.util.List;

public record GetRestaurantDetailResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        String phone,
        Double rating,
        int reviewCount,
        List<String> mainImageUrl,
        String menuImageUrl,
        String corkagePrice,
        List<String> corkageOptions,
        String representMenu,
        String pairingAlcohol,
        String pairingDescription,
        String pairingImageUrl,
        String openingHours,
        boolean scrap
) {
}