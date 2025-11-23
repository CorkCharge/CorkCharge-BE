package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.dto.response.GetHotRestaurantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotRestaurantResponseMapper {

    private final ImageRepository imageRepository;

    public GetHotRestaurantResponse toResponse(RestaurantSummary s) {

        return new GetHotRestaurantResponse(
                s.getRestaurantId(),
                s.getName(),
                s.getAddress(),
                s.getBookmarkCount(),
                s.getOpeningHours(),
                s.getMainImageUrl()
        );
    }
}
