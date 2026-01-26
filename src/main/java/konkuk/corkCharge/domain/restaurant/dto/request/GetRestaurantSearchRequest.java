package konkuk.corkCharge.domain.restaurant.dto.request;

public record GetRestaurantSearchRequest(
        String keyword,
        ClusterListSort sort
) { }
