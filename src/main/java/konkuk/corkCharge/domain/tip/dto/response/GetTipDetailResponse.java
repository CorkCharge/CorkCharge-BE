package konkuk.corkCharge.domain.tip.dto.response;

import konkuk.corkCharge.domain.tip.domain.TipCategory;

import java.time.LocalDateTime;
import java.util.List;

public record GetTipDetailResponse(
        Long tipId,
        String title,
        String content,
        TipCategory tipCategory,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
}
