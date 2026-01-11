package konkuk.corkCharge.domain.bookmark.dto.request;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkTargetType;

import java.util.List;

public record PostBookmarkRequest(
        BookmarkTargetType targetType,
        Long targetId,
        List<Long> groupIds // RESTAURANT 전용
) {}
