package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetClusterListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Stream;

import static konkuk.corkCharge.domain.corkageStore.domain.OptionType.ETC;

@Component
@RequiredArgsConstructor
public class ClusterListResponseMapper {

    public GetClusterListResponse.Item toItem(
            Restaurant restaurant,
            CorkageStore corkageStore,
            String[] imageUrls,
            boolean scrap
    ) {
        String corkagePrice = formatCorkagePrice(corkageStore);
        List<String> corkageOptions = decodeOptions(corkageStore.getOptionBits(), corkageStore.getEtcContent());

        return new GetClusterListResponse.Item(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getRating(),
                restaurant.getReviewCount(),
                restaurant.getOpeningHours(),
                corkagePrice,
                corkageOptions,
                imageUrls == null ? new String[0] : imageUrls,
                restaurant.getBookmarkCount(),
                scrap
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

        int bits = optionBits;

        return Stream.of(OptionType.values())
                .filter(type -> (bits & (1 << type.ordinal())) != 0)
                .map(type -> (type == ETC) ? etcContent : type.getLabel())
                .filter(opt -> opt != null && !opt.isBlank())
                .toList();
    }
}