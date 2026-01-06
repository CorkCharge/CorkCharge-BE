package konkuk.corkCharge.domain.bookmark.dto.response;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;

import java.time.LocalDateTime;
import java.util.List;

public record GetBookmarkGroupListResponse(
        int totalGroupCount,
        List<GroupDto> groups
) {
    public record GroupDto(
            Long groupId,
            String name,
            String color,
            BookmarkGroupVisibility visibility,
            int storeCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}