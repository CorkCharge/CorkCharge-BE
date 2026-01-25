package konkuk.corkCharge.domain.ownerRestaurant.dto.response;

import java.util.List;

public record GetOwnerMyRestaurantListResponse(
        List<Item> items
) {
    public record Item(
            Long restaurantId,
            String restaurantName,
            Double rating,
            Integer totalReviewCount,
            String openingHours,

            // hasCorkage=false면 null / 빈값 가능
            String corkagePrice,
            List<String> corkageOptions,

            List<String> mainImages
    ) { }
}
