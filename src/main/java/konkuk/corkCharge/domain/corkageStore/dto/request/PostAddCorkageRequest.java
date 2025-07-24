package konkuk.corkCharge.domain.corkageStore.dto.request;

import java.util.List;

public record PostAddCorkageRequest(
        Long userId,
        Long restaurantId,
        String CorkageType,
        int corkagePrice,
        List<MultiCorkageRequest> multiCorkages,
        List<String> optionTypes,
        String etcContent
) {
    public static PostAddCorkageRequest of(
            Long userId,
            Long restaurantId,
            String corkageType,
            int corkagePrice,
            List<MultiCorkageRequest> multiCorkages,
            List<String> optionTypes,
            String etcContent
    ) {
        return new PostAddCorkageRequest(
                userId,
                restaurantId,
                corkageType,
                corkagePrice,
                multiCorkages,
                optionTypes,
                etcContent
        );
    }
}