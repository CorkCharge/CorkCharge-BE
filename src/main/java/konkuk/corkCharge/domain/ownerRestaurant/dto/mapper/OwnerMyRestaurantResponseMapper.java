package konkuk.corkCharge.domain.ownerRestaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.ownerRestaurant.dto.response.GetOwnerMyRestaurantListResponse;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OwnerMyRestaurantResponseMapper {
    public GetOwnerMyRestaurantListResponse.Item toItem(RestaurantSummary summary) {

        String corkagePrice = summary.isHasCorkage() ? summary.getCorkagePrice() : null;

        List<String> corkageOptions = summary.isHasCorkage()
                ? decodeOptions(summary.getOptionBits(), summary.getOptionEtcContent())
                : List.of();

        return new GetOwnerMyRestaurantListResponse.Item(
                summary.getRestaurantId(),
                summary.getName(),
                safeDouble(summary.getAvgRating()),
                summary.getReviewCount() == null ? 0 : summary.getReviewCount(),
                summary.getOpeningHours(),

                corkagePrice,
                corkageOptions,

                safeList(summary.getMainImages())
        );
    }

    private List<String> safeList(List<String> v) {
        return v == null ? List.of() : v;
    }

    private double safeDouble(Double v) {
        return v == null ? 0.0 : v;
    }

    private List<String> decodeOptions(Integer optionBits, String etcContent) {
        if (optionBits == null || optionBits == 0) return List.of();

        int bits = optionBits;

        return Arrays.stream(OptionType.values())
                .filter(type -> (bits & (1 << type.ordinal())) != 0)
                .map(type -> type == OptionType.ETC ? etcContent : type.getLabel())
                .filter(s -> s != null && !s.isBlank())
                .toList();
    }
}
