package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.dto.response.GetHotRestaurantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.RESTAURANT;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;

@Component
@RequiredArgsConstructor
public class HotRestaurantResponseMapper {

    private final ImageRepository imageRepository;

    public GetHotRestaurantResponse toResponse(Restaurant restaurant) {

        String imageUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(RESTAURANT, restaurant.getRestaurantId(), MAIN)
                .map(Image::getImageUrl)
                .orElse(null);

        int bookmarkCount = restaurant.getBookmarkCount() != null
                ? restaurant.getBookmarkCount()
                : 0;

        return new GetHotRestaurantResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                bookmarkCount,
                restaurant.getOpeningHours(),
                imageUrl
        );
    }
}
