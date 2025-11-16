package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetMapRestaurantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapRestaurantResponseMapper {

    private final CorkageStoreRepository corkageStoreRepository;

    public GetMapRestaurantResponse toResponse(Restaurant r) {
        CorkageStore cs = corkageStoreRepository
                .findByRestaurant_RestaurantId(r.getRestaurantId())
                .orElse(null);

        String price = extractPrice(cs);

        return new GetMapRestaurantResponse(
                r.getRestaurantId(),
                r.getLatitude(),
                r.getLongitude(),
                price
        );
    }

    private String extractPrice(CorkageStore cs) {
        if (cs == null || cs.getCorkageType() == null) {
            return null;
        }

        CorkageType type = cs.getCorkageType();

        return switch (type) {
            case FREE -> "Free";
            case PER_BOTTLE -> "병당 " + cs.getCorkagePrice();
            case PER_PERSON -> "인당 " + cs.getCorkagePrice();
            case PER_TABLE -> "테이블 " + cs.getCorkagePrice();
            case MULTIPLE -> "다중 콜키지";
        };
    }
}
