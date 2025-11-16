package konkuk.corkCharge.domain.tip.dto.mapper;

import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.dto.response.GetTipDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.TIP;

@Component
@RequiredArgsConstructor
public class TipDetailResponseMapper {

    private final ImageRepository imageRepository;

    public GetTipDetailResponse toResponse(Tip tip) {

        List<String> urls = imageRepository
                .findUrlsByCategoryAndTypeId(TIP, tip.getTipId());

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