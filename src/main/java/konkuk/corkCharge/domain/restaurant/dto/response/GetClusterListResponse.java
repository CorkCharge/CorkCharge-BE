package konkuk.corkCharge.domain.restaurant.dto.response;

import java.util.List;

public record GetClusterListResponse(
        int totalCount,
        List<Item> restaurants
) {
    public record Item(
            Long restaurantId,
            String name,
            Double rating,
            int reviewCount,
            String openingHours,
            String corkagePrice,
            List<String> corkageOptions,
            String[] imageUrls,
            Integer bookmarkCount,
            boolean scrap
    ) {}
}
