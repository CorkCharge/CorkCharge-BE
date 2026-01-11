package konkuk.corkCharge.domain.bookmark.dto.response;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;

public record PutBookmarkGroupResponse(
        Long groupId,
        String name,
        String color,
        BookmarkGroupVisibility visibility,
        Integer displayOrder
) {
}