package konkuk.corkCharge.domain.bookmark.dto.response;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;

import java.time.LocalDateTime;

public record GetRestaurantBookmarkGroupItemResponse(
        Long groupId,
        String name,
        String color,
        BookmarkGroupVisibility visibility,
        boolean storedFlag,
        long storeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
