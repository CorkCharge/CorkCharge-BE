package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.dto.response.GetHomeRestaurantResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static konkuk.corkCharge.domain.corkageStore.domain.OptionType.ETC;

@Component
public class HomeRestaurantResponseMapper {

    public GetHomeRestaurantResponse toResponse(
            RestaurantSummary summary,
            Double distanceKm,
            boolean scrap
    ) {
        List<String> corkageOptions = decodeOptions(summary.getOptionBits(), summary.getOptionEtcContent());

        return new GetHomeRestaurantResponse(
                summary.getRestaurantId(),
                summary.getName(),
                summary.getAddress(),
                summary.getAvgRating(),
                summary.getReviewCount() == null ? 0 : summary.getReviewCount(),
                summary.getCorkagePrice(),
                corkageOptions,
                distanceKm,
                summary.getMainImageUrl(),
                summary.getOpeningHours(),
                scrap
        );
    }

    private List<String> decodeOptions(Integer optionBits, String etcContent) {
        if (optionBits == null || optionBits == 0)
            return List.of();

        int bits = optionBits;

        return Stream.of(OptionType.values())
                .filter(type -> (bits &  (1 << type.ordinal())) != 0)
                .map(type -> type == ETC ? etcContent : type.getLabel())
                .filter(opt -> opt != null && !opt.isBlank())
                .toList();
    }

}
