package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;

import java.util.List;

public record GetGroupRestaurantPinsResponse(
        int totalGroupCount,
        List<GroupPins> groups
) {
    public record GroupPins(
       Long groupId,
       String name,
       String color,
       BookmarkGroupVisibility visibility,
       int storeCount,
       List<Pin> pins
    ) {}

    public record Pin(
       Long restaurantId,
       Double lat,
       Double lon,
       String corkagePrice
    ) {}
}
