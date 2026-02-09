package konkuk.corkCharge.domain.bookmark.dto.response;

import java.util.List;

public record GetRestaurantBookmarkGroupListResponse(
        int totalGroupCount,
        List<GetRestaurantBookmarkGroupItemResponse> groups
) {
}
