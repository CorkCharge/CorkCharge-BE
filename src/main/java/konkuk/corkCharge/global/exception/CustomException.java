package konkuk.corkCharge.global.exception;

import konkuk.corkCharge.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private final ResponseStatus exceptionStatus;

    public CustomException(ResponseStatus exceptionStatus){
        super(exceptionStatus.getMessage());
        this.exceptionStatus=exceptionStatus;
    }
}