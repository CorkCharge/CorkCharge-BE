package konkuk.corkCharge.domain.tip.dto.response;

import konkuk.corkCharge.domain.tip.domain.TipCategory;

public record GetTipListResponse(
        Long tipId,
        String title,
        TipCategory tipCategory,
        String imageUrl
) {
}
