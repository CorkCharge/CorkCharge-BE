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
    CATEGORY_NOT_FOUND(60005, "올바르지 않은 카테고리입니다."),
    LOCATION_REQUIRED(60006, "사용자 위치(좌표)값은 필수입니다."),

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
    FORBIDDEN_REVIEW_EDIT(100002, "해당 리뷰를 수정할 권한이 없습니다."),

    /**
     * 110000 : Tip
     */
    TIP_NOT_FOUND(110000, "해당 tip을 찾을 수 없습니다."),

    /**
     * 120000 : Bookmark
     */
    BOOKMARK_NOT_FOUND(120000, "해당 북마크를 찾을 수 없습니다."),
    GROUP_NOT_FOUND(120001, "해당 북마크 그룹을 찾을 수 없습니다."),
    GROUP_FORBIDDEN(120002, "해당 북마크 그룹에 대한 권한이 없습니다."),
    BOOKMARK_ALREADY_EXISTS(120003, "이미 저장된 매장입니다."),
    GROUP_NAME_SAME_AS_BEFORE(120004, "기존 그룹명과 동일합니다."),
    GROUP_NAME_ALREADY_EXISTS(120005, "이미 존재하는 그룹명입니다."),

    /**
     * 130000 : Suggestion
     */
    SUGGESTION_NOT_FOUND(130000, "해당 문의하기를 찾을 수 없습니다."),

    /**
     * 140000 : Auth/JWT
     */
    AUTH_REQUIRED(140000, "인증이 필요합니다."),
    INVALID_AUTH_TYPE(140001, "유효하지 않은 인증 타입입니다."),
    JWT_INVALID(140002, "유효하지 않은 토큰입니다."),
    JWT_REFRESH_NOT_FOUND(140003, "리프레시 토큰을 찾을 수 없습니다."),
    JWT_REFRESH_TOKEN_MISMATCH(140004, "저장된 리프레시 토큰과 일치하지 않습니다."),
    JWT_EXPIRED(140005, "토큰이 만료되었습니다."),

    /**
     * 150000 : OAuth(Naver)
     */
    NAVER_TOKEN_ERROR(150000, "네이버 토큰 응답 오류입니다."),
    NAVER_TOKEN_REQUEST_FAILED(150001, "네이버 토큰 요청에 실패했습니다."),
    NAVER_USER_INFO_REQUEST_FAILED(150002, "네이버 사용자 정보 요청에 실패했습니다."),

    /**
     * 160000 : helprequest
     */
    ALREADY_COMPLETED_HELP_REQUEST(160000, "이미 완료한 요청입니다."),
    HELP_REQUEST_NOT_FOUND(160001, "기존 요청을 찾을 수 없습니다.")
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
