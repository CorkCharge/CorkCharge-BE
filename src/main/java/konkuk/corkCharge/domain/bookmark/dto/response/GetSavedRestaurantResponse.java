package konkuk.corkCharge.domain.bookmark.dto.response;

import konkuk.corkCharge.domain.bookmark.domain.Bookmark;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.CORKAGE;

public record GetSavedRestaurantResponse(
        Long bookmarkId,
        Long restaurantId,
        String name,
        String address,
        String thumbnailUrl,
        double rating,
        int reviewCount,
        int bookmarkCount,
        boolean hasCorkage,
        String corkagePrice,
        LocalDateTime createdAt
) {
    public static GetSavedRestaurantResponse from(Bookmark bookmark,
                                                  Restaurant restaurant,
                                                  List<Image> images,
                                                  CorkageStore corkageStore){
        String thumbnail = images.stream()
                .map(Image::getImageUrl)
                .findFirst()
                .orElse("");


        String corkagePrice = null;

        if (corkageStore != null) {
            // 콜키지 가격
            if (corkageStore.getCorkageType() == CorkageType.MULTIPLE) {
                corkagePrice = corkageStore.getMultiPrices().stream()
                        .map(mp -> mp.getLiquorType() + " 병당: " + mp.getPrice() + "원")
                        .collect(Collectors.joining(", "));
            } else if (corkageStore.getCorkageType() == CorkageType.FREE) {
                corkagePrice = "콜키지 프리";
            } else {
                corkagePrice = switch (corkageStore.getCorkageType()) {
                    case PER_BOTTLE -> "병당 " + corkageStore.getCorkagePrice() + "원";
                    case PER_PERSON -> "인당 " + corkageStore.getCorkagePrice() + "원";
                    case PER_TABLE -> "테이블당 " + corkageStore.getCorkagePrice() + "원";
                    default -> null;
                };
            }

        }
        return new GetSavedRestaurantResponse(
                bookmark.getId(),
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                thumbnail,
                restaurant.getRating(),
                restaurant.getReviewCount(),
                restaurant.getBookmarkCount(),
                restaurant.isHasCorkage(),
                corkagePrice,
                bookmark.getCreatedAt()
        );
    }
}
