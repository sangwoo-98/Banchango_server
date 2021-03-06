package com.banchango.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends ApiException{
    public InternalServerErrorException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerErrorException() {
        this("Internal Server Error Exception.");
    }
}
