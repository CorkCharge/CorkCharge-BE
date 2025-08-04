package konkuk.corkCharge.domain.tip.dto.response;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.domain.TipCategory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record GetTipListResponse(
        Long tipId,
        String title,
        String content,
        TipCategory tipCategory,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
    public static GetTipListResponse from(Tip tip){
        List<String> imageUrls = tip.getImages().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());

        return GetTipListResponse.builder()
                .tipId(tip.getTipId())
                .title(tip.getTitle())
                .content(tip.getContent())
                .tipCategory(tip.getTipCategory())
                .imageUrls(imageUrls)
                .createdAt(tip.getCreatedAt())
                .build();

    }
}
