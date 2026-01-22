package konkuk.corkCharge.domain.helpRequest.dto.request;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;

public record PostHelpRequestDetailRequest(
        Long restaurantId,
        CorkageType corkageType,
        Integer preferredPrice,
        OptionType firstPriority,
        OptionType secondPriority,
        String etc
) {
}