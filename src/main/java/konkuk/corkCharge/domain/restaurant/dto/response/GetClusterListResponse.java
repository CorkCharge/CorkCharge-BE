package konkuk.corkCharge.domain.restaurant.dto.response;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

import java.util.List;

import static konkuk.corkCharge.domain.corkageStore.domain.OptionType.ETC;
import static konkuk.corkCharge.domain.image.domain.ImageCategory.RESTAURANT;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;

public record GetClusterListResponse(
        Long restaurantId,
        String name,
        Double rating,
        int reviewCount,
        String corkagePrice,
        List<String> corkageOptions,
        String imageUrl
) {
    public static GetClusterListResponse from(Restaurant restaurant) {
        CorkageStore corkageStore = restaurant.getCorkageStore();

        String corkagePrice = switch (corkageStore.getCorkageType()) {
            case FREE -> "FREE";
            case MULTIPLE -> {
                int min = corkageStore.getMultiPrices().stream()
                        .mapToInt(MultiCorkage::getPrice)
                        .min()
                        .orElse(Integer.MAX_VALUE);
                yield min == Integer.MAX_VALUE ? "가격 미정" : "최저 " + min + "원";
            }
            case PER_BOTTLE -> "병당 " + corkageStore.getCorkagePrice() + "원";
            case PER_PERSON -> "인당 " + corkageStore.getCorkagePrice() + "원";
            case PER_TABLE -> "테이블당 " + corkageStore.getCorkagePrice() + "원";
        };

        List<String> corkageOptions = corkageStore.getCorkageOptions().stream()
                .map(opt -> {
                    if (opt.getOptionType() == ETC)
                        return opt.getEtcContent();
                    return opt.getOptionType().getLabel();
                })
                .toList();

        String imageUrl = restaurant.getImages().stream()
                .filter(img -> img.getCategory() == RESTAURANT && img.getType() == MAIN)
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);

        return new GetClusterListResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getRating(),
                restaurant.getReviewCount(),
                corkagePrice,
                corkageOptions,
                imageUrl
        );
    }
}
