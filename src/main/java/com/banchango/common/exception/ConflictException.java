package com.banchango.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends ApiException{
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException() {
        this("Conflict Exception.");
    }
}
