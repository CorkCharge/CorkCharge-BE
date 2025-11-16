package konkuk.corkCharge.domain.restaurant.dto.response;

import java.util.List;

public record GetClusterListResponse(
        Long restaurantId,
        String name,
        Double rating,
        int reviewCount,
        String corkagePrice,
        List<String> corkageOptions,
        String imageUrl
) {}
