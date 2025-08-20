package konkuk.corkCharge.domain.bookmark.dto.request;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;

public record DeleteBookmarkRequest(
        Long targetId,
        BookmarkTargetType targetType
) {
}
