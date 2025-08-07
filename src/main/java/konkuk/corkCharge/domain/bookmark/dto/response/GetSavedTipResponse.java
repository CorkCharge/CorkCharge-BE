package konkuk.corkCharge.domain.bookmark.dto.response;

import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.domain.TipCategory;

import java.time.LocalDateTime;

public record GetSavedTipResponse(
        Long bookmarkId,
        Long tipId,
        String title,
        TipCategory tipCategory,
        String imageUrl,
        LocalDateTime createdAt
) {
}
