package konkuk.corkCharge.domain.restaurant.dto.request;

public record GetCategoryRestaurantRequest(
        String category,    // 중국요리, 회, 이탈리안, 초밥, 육류,고기
        Double lat,
        Double lon
) {
    public boolean hasUserLocation() {
        return lat != null && lon != null;
    }
}
