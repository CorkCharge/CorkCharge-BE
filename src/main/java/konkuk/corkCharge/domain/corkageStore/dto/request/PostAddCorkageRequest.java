package konkuk.corkCharge.domain.corkageStore.dto.request;

import java.util.List;

public record PostAddCorkageRequest(
        Long restaurantId,
        String CorkageType,
        int corkagePrice,
        List<MultiCorkageRequest> multiCorkages,
        List<String> optionTypes,
        String etcContent
) {
    public static PostAddCorkageRequest of(
            Long restaurantId,
            String corkageType,
            int corkagePrice,
            List<MultiCorkageRequest> multiCorkages,
            List<String> optionTypes,
            String etcContent
    ) {
        return new PostAddCorkageRequest(
                restaurantId,
                corkageType,
                corkagePrice,
                multiCorkages,
                optionTypes,
                etcContent
        );
    }
}