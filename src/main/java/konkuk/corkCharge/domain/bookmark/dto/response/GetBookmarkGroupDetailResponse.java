package konkuk.corkCharge.domain.bookmark.dto.response;

import java.util.List;

public record GetBookmarkGroupDetailResponse(
        String groupName,
        int totalCount,
        List<RestaurantDto> restaurants
) {
    public record RestaurantDto(
            Long restaurantId,
            String name,
            double rating,
            int reviewCount,
            String openingHoursText,
            List<String> imageUrls,
            String corkagePrice,
            String corkageOption
    ) {
    }
}