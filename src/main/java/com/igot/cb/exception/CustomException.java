package com.igot.cb.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class CustomException extends RuntimeException {

    private final String code;
    private final String message;
    private final String httpStatusCode;
    private final Map<String, String> errors;

    public CustomException(String code, String message) {
        this(code, message, null, null);
    }

    public CustomException(String code, String message, String httpStatusCode) {
        this(code, message, httpStatusCode, null);
    }

    public CustomException(Map<String, String> errors) {
        this(null, errors.toString(), null, errors);
    }

    private CustomException(String code, String message, String httpStatusCode, Map<String, String> errors) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
        this.errors = errors;
    }
}
