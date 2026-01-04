package konkuk.corkCharge.domain.bookmark.dto.request;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;

public record PostBookmarkGroupRequest(
        String name,
        String color,
        BookmarkGroupVisibility visibility
) {
}
