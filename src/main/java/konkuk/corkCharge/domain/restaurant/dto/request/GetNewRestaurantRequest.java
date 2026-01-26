package konkuk.corkCharge.domain.restaurant.dto.request;

import java.util.List;

public record GetNewRestaurantRequest(
        Double lat,
        Double lon,

        // 지역 필터
        String sido,
        String sigungu,
        List<String> dongList
) {
    public boolean hasUserLocation() {
        return lat != null && lon != null;
    }
}
