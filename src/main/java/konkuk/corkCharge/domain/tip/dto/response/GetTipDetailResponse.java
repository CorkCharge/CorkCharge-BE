package konkuk.corkCharge.domain.tip.dto.response;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.domain.TipCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record GetTipDetailResponse(
        Long tipId,
        String title,
        String content,
        TipCategory tipCategory,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
    public static GetTipDetailResponse from(Tip tip){
        List<String> urls = tip.getImages().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());

        return new GetTipDetailResponse(
                tip.getTipId(),
                tip.getTitle(),
                tip.getContent(),
                tip.getTipCategory(),
                urls,
                tip.getCreatedAt()
        );
    }
}
