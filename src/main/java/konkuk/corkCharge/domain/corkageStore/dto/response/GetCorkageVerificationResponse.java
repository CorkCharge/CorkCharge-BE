package konkuk.corkCharge.domain.corkageStore.dto.response;

public record GetCorkageVerificationResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        String thumbnailUrl
) {
}
