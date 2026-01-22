package konkuk.corkCharge.domain.helpRequest.dto.request;

import jakarta.validation.constraints.NotNull;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;

public record PostHelpRequestDetailRequest(
        @NotNull(message = "restaurantId는 필수입니다.")
        Long restaurantId,

        @NotNull(message = "corkageType은 필수입니다.")
        CorkageType corkageType,

        @NotNull(message = "preferredPrice는 필수입니다.")
        Integer preferredPrice,

        @NotNull(message = "firstPriority는 필수입니다.")
        OptionType firstPriority,

        @NotNull(message = "secondPriority는 필수입니다.")
        OptionType secondPriority,

        String content
) {
}