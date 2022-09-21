package com.sse.core.exception;

import lombok.Getter;

public class BaseException extends RuntimeException {

    @Getter
    private ErrorType errorType;

    public BaseException(String msg) {
        super(msg);
    }

    public BaseException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public BaseException(ErrorType errorType, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
    }

}
