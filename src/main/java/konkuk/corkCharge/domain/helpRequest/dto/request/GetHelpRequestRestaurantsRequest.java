package konkuk.corkCharge.domain.helpRequest.dto.request;

import java.util.List;

public record GetHelpRequestRestaurantsRequest(
        String sido,
        String sigungu,
        List<String> dong,
        String keyword
) {
}
