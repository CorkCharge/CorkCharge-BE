package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.dto.response.HomeRestaurantCard;
import org.springframework.stereotype.Component;

@Component
public class HomeRestaurantCardMapper {

    public HomeRestaurantCard toCard(RestaurantSummary summary) {
        return new HomeRestaurantCard(
                summary.getRestaurantId(),
                summary.getName(),
                summary.getAvgRating(),
                summary.getReviewCount() == null ? 0 : summary.getReviewCount(),
                summary.getCorkagePrice(),
                summary.getMainImageUrl()
        );
    }
}
