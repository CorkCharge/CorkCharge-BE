package konkuk.corkCharge.domain.user.dto.response;

import java.time.LocalDate;
import java.util.List;

public record GetMyHelpRequestsResponse(
        List<HelpRequestSummary> helprequests
) {
    public record HelpRequestSummary(
            Long helprequestId,
            String restaurantName,
            LocalDate createdAt
    ) {}
}
