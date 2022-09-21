package com.sse.core.exception;

import lombok.Getter;

public class MemberException extends BaseException {

    @Getter
    private ErrorType errorType;

    public MemberException(String msg) {
        super(msg);
    }

    public MemberException(ErrorType errorType) {
        super(errorType);
        this.errorType = errorType;
    }

    public MemberException(ErrorType errorType, Throwable cause) {
        super(errorType, cause);
        this.errorType = errorType;
    }

}
