package konkuk.corkCharge.domain.user.dto.response;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;

import java.time.LocalDate;

public record GetMyHelpRequestDetailResponse(
        Long helprequestId,
        String restaurantName,
        CorkageType corkageType,
        Integer preferredPrice,
        OptionType firstPriority,
        OptionType secondPriority,
        String content,
        LocalDate createdAt
) {
}