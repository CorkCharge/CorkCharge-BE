package konkuk.corkCharge.domain.restaurant.dto.response;

public record PostRestaurantGeocodingResponse(
        int requestedLimit,
        int targetCount,
        int successCount,
        int notFoundCount,
        int failedCount
) {
}
