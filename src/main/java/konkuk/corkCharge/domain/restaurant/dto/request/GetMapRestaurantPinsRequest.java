package konkuk.corkCharge.domain.restaurant.dto.request;

import java.util.List;

public record GetMapRestaurantPinsRequest(
        // 지도 범위
        double latMin,
        double latMax,
        double lonMin,
        double lonMax,

        // 검색
        String keyword,

        // 지역 필터
        String sido,
        String sigungu,
        List<String> dongList,

        // 콜키지 필터
        Double minScore, Double maxScore,
        Integer minBottlePrice, Integer maxBottlePrice,     // PER_BOTTLE
        Integer minPersonPrice, Integer maxPersonPrice,     // PER_PERSON
        Integer minTablePrice, Integer maxTablePrice,       // PER_TABLE
        List<String> optionTypes,
        List<String> corkageTypes
) {
}
