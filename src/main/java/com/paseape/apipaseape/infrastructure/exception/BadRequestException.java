package com.paseape.apipaseape.infrastructure.exception;

import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;

public class BadRequestException extends Exception {
    public Integer statusCode;
    public String code;

    public BadRequestException() {}

    public BadRequestException(String message) {
        super(message);
        this.statusCode = StatusCodes.Code400;
        this.code = MessageCodes.ResponseCodeBR01;
    }

    public BadRequestException(String message, Exception inner) {
        super(message, inner);
    }

    public BadRequestException(String code, String message) {
        super(message);
        this.statusCode = StatusCodes.Code400;
        this.code = code;
    }
}
