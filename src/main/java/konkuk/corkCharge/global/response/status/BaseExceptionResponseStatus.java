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
    RESTAURANT_NOT_FOUND(60000, "매장 정보를 찾을 수 없습니다."),
    CORKAGE_RESTAURANT_NOT_FOUND(60001, "콜키지 매장이 없습니다."),
    SIDO_REQUIRED(60002, "시•도 입력은 필수입니다."),
    NOT_EXIT_TYPE(60003, "유효하지 않은 type 입니다."),
    ALREADY_REGISTERED_CORKAGE(60004, "해당 매장은 이미 콜키지 등록이 돼 있습니다."),

    /**
     * 70000 : User
     */
    USER_NOT_FOUND(70000, "해당 사용자를 찾을 수 없습니다."),
    PERMISSION_DENIED(70001, "ADMIN 혹은 OWNER 권한이 필요합니다."),

    /**
     * 80000 : Image
     */
    FAILED_UPLOAD(80000, "S3 업로드를 실패했습니다."),
    FAILED_DELETE_IMAGE(80001, "S3 이미지 삭제에 실패하였습니다."),

    /**
     * 90000 : OwnerRestaurant
     */
    ALREADY_REGISTERED_BY_ANOTHER_OWNER(90000, "이미 다른 사장님이 등록한 식당입니다."),

    /**
     * 100000 : Review
     */
    INVALID_RANGE(100000, "유효하지 않은 기간 필터입니다."),
    REVIEW_NOT_FOUND(100001, "해당 리뷰를 찾을 수 없습니다."),
    FORBIDDEN_REVIEW_EDIT(100002, "해당 리뷰를 수정할 권한이 없습니다.")
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
