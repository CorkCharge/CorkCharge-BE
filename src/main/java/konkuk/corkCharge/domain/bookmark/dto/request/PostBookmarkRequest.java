package konkuk.corkCharge.domain.bookmark.dto.request;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;
import lombok.Getter;

public record PostBookmarkRequest(
        Long targetId,
        BookmarkTargetType targetType
) {
}
