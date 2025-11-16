package konkuk.corkCharge.domain.tip.dto.mapper;

import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.dto.response.GetTipListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.TIP;

@Component
@RequiredArgsConstructor
public class TipListResponseMapper {

    private final ImageRepository imageRepository;

    public GetTipListResponse toResponse(Tip tip) {

        String imageUrl = imageRepository
                .findFirstByCategoryAndTypeId(TIP, tip.getTipId())
                .map(img -> img.getImageUrl())
                .orElse("");

        return new GetTipListResponse(
                tip.getTipId(),
                tip.getTitle(),
                tip.getTipCategory(),
                imageUrl
        );
    }
}
