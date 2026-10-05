package com.igot.cb.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void shouldReturnBadRequestWithGivenHttpStatusCodeForCustomException() {
        CustomException ex = new CustomException("SOME_CODE", "some message", "404");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("SOME_CODE");
        assertThat(response.getBody().getMessage()).isEqualTo("some message");
        assertThat(response.getBody().getHttpStatusCode()).isEqualTo("404");
    }

    @Test
    void shouldDefaultHttpStatusCodeWhenNotProvidedOnCustomException() {
        CustomException ex = new CustomException("SOME_CODE", "some message");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHttpStatusCode()).isEqualTo(String.valueOf(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void shouldNotLogWhenCustomExceptionMessageIsBlank() {
        CustomException ex = new CustomException("SOME_CODE", "");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnInternalServerErrorForGenericException() {
        Exception ex = new RuntimeException("unexpected failure");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("unexpected failure");
    }
}
