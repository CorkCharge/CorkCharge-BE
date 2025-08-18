package konkuk.corkCharge.domain.tip.dto.request;

import konkuk.corkCharge.domain.tip.domain.TipCategory;

public record PostTipRequest(
        String title,
        String content,
        TipCategory tipCategory
) {
}
