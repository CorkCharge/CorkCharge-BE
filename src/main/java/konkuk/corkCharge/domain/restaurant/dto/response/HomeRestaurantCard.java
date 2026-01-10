package konkuk.corkCharge.domain.restaurant.dto.response;

public record HomeRestaurantCard(
        Long restaurantId,
        String restaurantName,
        Double rating,
        Integer reviewCount,
        String corkagePrice,
        String mainImageUrls
) {
}
