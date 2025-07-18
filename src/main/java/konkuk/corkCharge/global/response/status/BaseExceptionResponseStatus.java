package konkuk.corkCharge.global.response.status;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BaseExceptionResponseStatus implements ResponseStatus {

    SUCCESS(20000, "요청에 성공했습니다."),
    BAD_REQUEST(40000, "유효하지 않은 요청입니다."),
    NOT_FOUND(40400, "존재하지 않는 API입니다."),
    INTERNAL_SERVER_ERROR(50000, "서버 내부 오류입니다."),

    /**
     * 30000 : openApi
     */
    RESTAURANT_API_ERROR(30000, "공공데이터 포탈 API 호출 중 오류 발생"),


    /**
     * 60000 : restaurant
     */
    RESTAURANT_NOT_FOUND(60000, "식당 정보를 찾을 수 없습니다."),

    /**
     * 70000 : User 도메인 에러 코드
     */


    USER_NOT_FOUND(70001, "해당 사용자를 찾을 수 없습니다.")
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
