package konkuk.corkCharge.domain.tip.dto.response;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.domain.TipCategory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record GetTipListResponse(
        Long tipId,
        String title,
        TipCategory tipCategory,
        String imageUrl
) {
    public static GetTipListResponse from(Tip tip){
        String imageUrl = tip.getImages().stream()
                .map(img -> img.getImageUrl())
                .findFirst()
                .orElse("");

        return new GetTipListResponse(
                tip.getTipId(),
                tip.getTitle(),
                tip.getTipCategory(),imageUrl
        );
    }
}
