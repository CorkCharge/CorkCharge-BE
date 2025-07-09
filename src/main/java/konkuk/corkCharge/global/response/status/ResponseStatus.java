package konkuk.corkCharge.global.response.status;

public interface ResponseStatus {

    boolean getSuccess();
    int getCode();
    String getMessage();
}
