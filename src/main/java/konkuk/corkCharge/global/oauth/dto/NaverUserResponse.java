package konkuk.corkCharge.global.oauth.dto;

public record NaverUserResponse(
        String resultcode,
        String message,
        Response response
) {
    public record Response(
            String id,
            String email,
            String name
    ) {}
}
