package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetRestaurantSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchRestaurantResponseMapper {

    public GetRestaurantSearchResponse.Item toItem(
            Restaurant restaurant,
            CorkageStore corkageStore,
            String[] imageUrls,
            boolean scrap
    ) {
        String corkagePrice = null;
        List<String> corkageOptions = null;

        if (corkageStore != null && corkageStore.getCorkageType() != null) {
            corkagePrice = formatCorkagePrice(corkageStore);
            corkageOptions = decodeOptions(
                    corkageStore.getOptionBits(),
                    corkageStore.getEtcContent()
            );
        }

        return new GetRestaurantSearchResponse.Item(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getRating(),
                restaurant.getReviewCount(),
                restaurant.getOpeningHours(),
                corkagePrice,
                corkageOptions,
                imageUrls == null ? new String[0] : imageUrls,
                scrap,
                restaurant.getLatitude(),
                restaurant.getLongitude()
        );
    }

    private String formatCorkagePrice(CorkageStore cs) {
        return switch (cs.getCorkageType()) {
            case FREE -> "FREE";
            case MULTIPLE -> {
                int min = cs.getMultiPrices().stream()
                        .mapToInt(MultiCorkage::getPrice)
                        .min()
                        .orElse(Integer.MAX_VALUE);
                yield (min == Integer.MAX_VALUE) ? "가격 미정" : "최저 " + min + "원";
            }
            case PER_BOTTLE -> "병당 " + cs.getCorkagePrice() + "원";
            case PER_PERSON -> "인당 " + cs.getCorkagePrice() + "원";
            case PER_TABLE -> "테이블당 " + cs.getCorkagePrice() + "원";
        };
    }

    private List<String> decodeOptions(Integer optionBits, String etcContent) {
        if (optionBits == null || optionBits == 0) return List.of();

        return Arrays.stream(OptionType.values())
                .filter(type -> (optionBits & (1 << type.ordinal())) != 0)
                .map(type -> type == OptionType.ETC ? etcContent : type.getLabel())
                .filter(s -> s != null && !s.isBlank())
                .toList();
    }
}
