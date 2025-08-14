package konkuk.corkCharge.domain.corkageStore.dto.response;

public record PostAdminCorkageResponse(
        Long restaurantId,
        String name,
        String address,
        String thumbnialUrl
) {
}
