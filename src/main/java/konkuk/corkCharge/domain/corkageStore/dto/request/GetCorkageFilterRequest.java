package konkuk.corkCharge.domain.corkageStore.dto.request;

import java.util.List;

public record GetCorkageFilterRequest(
        Double minScore, Double maxScore,
        Integer minBottlePrice, Integer maxBottlePrice,     // PER_BOTTLE
        Integer minPersonPrice, Integer maxPersonPrice,     // PER_PERSON
        Integer minTablePrice, Integer maxTablePrice,       // PER_TABLE
        List<String> optionTypes,
        List<String> corkageTypes
) {
    public static GetCorkageFilterRequest of(
            Double minScore, Double maxScore,
            Integer minBottlePrice, Integer maxBottlePrice,
            Integer minPersonPrice, Integer maxPersonPrice,
            Integer minTablePrice, Integer maxTablePrice,
            List<String> optionTypes,
            List<String> corkageTypes
    ) {
        return new GetCorkageFilterRequest(
                minScore, maxScore,
                minBottlePrice, maxBottlePrice,
                minPersonPrice, maxPersonPrice,
                minTablePrice, maxTablePrice,
                optionTypes,
                corkageTypes
        );
    }
}
