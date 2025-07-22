package konkuk.corkCharge.domain.restaurant.dto.request;

import java.util.List;

public record GetFilterRequest(
        String type,
        String sido,
        String sigungu,
        List<String> dongList
) { }
