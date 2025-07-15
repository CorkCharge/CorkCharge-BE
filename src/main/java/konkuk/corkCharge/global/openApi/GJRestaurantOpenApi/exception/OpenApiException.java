package konkuk.corkCharge.global.openApi.GJRestaurantOpenApi.exception;

import konkuk.corkCharge.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OpenApiException implements ResponseStatus {
    RESTAURANT_API_ERROR(30000, "공공데이터 포탈 API 호출 중 오류 발생")
    ;

    private final boolean success = false;
    private final int code;
    private final String message;

    @Override
    public boolean getSuccess() {
        return success;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
