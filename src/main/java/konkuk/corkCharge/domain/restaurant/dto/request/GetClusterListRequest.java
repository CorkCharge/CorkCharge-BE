package konkuk.corkCharge.domain.restaurant.dto.request;

import java.util.List;

public record GetClusterListRequest(
        List<Long> restaurantIds
) {
}
