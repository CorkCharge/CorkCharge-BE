package konkuk.corkCharge.domain.bookmark.dto.request;

import java.util.List;

public record PutRestaurantBookmarkGroupsRequest(
        Long restaurantId,
        List<Long> groupIds
) {
}