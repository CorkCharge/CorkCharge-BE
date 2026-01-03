package konkuk.corkCharge.domain.restaurant.dto.request;

public record GetNewRestaurantRequest(
        Double lat,
        Double lon
) {
    public boolean hasUserLocation() {
        return lat != null && lon != null;
    }
}
