package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

public record GetMapRestaurantResponse(
        Long restaurantId,
        Double latitude,
        Double longitude,
        String price
) {
    public static GetMapRestaurantResponse from(Restaurant r) {
        return new GetMapRestaurantResponse(
                r.getRestaurantId(),
                r.getLatitude(),
                r.getLongitude(),
                extractPrice(r)
        );
    }

    private static String extractPrice(Restaurant r) {
        CorkageStore cs = r.getCorkageStore();

        return switch (cs.getCorkageType()) {
            case FREE -> "Free";
            case PER_BOTTLE -> "병당 " + cs.getCorkagePrice();
            case PER_PERSON -> "인당 " + cs.getCorkagePrice();
            case PER_TABLE -> "테이블 " + cs.getCorkagePrice();
            case MULTIPLE -> "다중 콜키지";
        };
    }
}
