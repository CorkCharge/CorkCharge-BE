package konkuk.corkCharge.domain.helpRequest.dto.request;

public record PostCorkageRequest(
        Long restaurantId,
        String content
) {
}
