package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RestaurantDetailResponseMapper {

    public GetRestaurantDetailResponse toResponse(RestaurantSummary summary) {

        Long restaurantId = summary.getRestaurantId();

        // 옵션 비트 → 문자열 리스트
        List<String> corkageOptions = decodeCorkageOptions(
                summary.getOptionBits(),
                summary.getOptionEtcContent()
        );

        List<String> mainImageUrls = summary.getMainImages() == null ? List.of() : summary.getMainImages();

        return new GetRestaurantDetailResponse(
                restaurantId,
                summary.getName(),
                summary.getAddress(),
                summary.getPhone(),
                summary.getAvgRating(),
                summary.getReviewCount() != null ? summary.getReviewCount() : 0,
                mainImageUrls,
                summary.getMenuImageUrl(),
                summary.getCorkagePrice(),
                corkageOptions,
                summary.getRepresentMenu(),
                summary.getPairingAlcohol(),
                summary.getPairingDescription(),
                summary.getPairingImageUrl(),
                summary.getOpeningHours()
        );
    }

    private List<String> decodeCorkageOptions(Integer optionBits, String etcContent) {
        if (optionBits == null) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        for (OptionType type : OptionType.values()) {
            // 해당 비트가 켜져 있으면
            if ((optionBits & (1 << type.ordinal())) != 0) {

                if (type == OptionType.ETC) {
                    if (etcContent != null && !etcContent.isBlank()) {
                        result.add(etcContent);
                    }
                } else {
                    result.add(type.getLabel());
                }
            }
        }

        return result;
    }
}