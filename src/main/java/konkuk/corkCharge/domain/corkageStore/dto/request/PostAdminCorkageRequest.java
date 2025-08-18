package konkuk.corkCharge.domain.corkageStore.dto.request;

public record PostAdminCorkageRequest(
        Long restaurantId,
        String name,
        String address
) {
}
