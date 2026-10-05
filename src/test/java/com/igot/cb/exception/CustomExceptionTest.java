package com.igot.cb.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExceptionTest {

    @Test
    void shouldBuildExceptionWithCodeAndMessage() {
        CustomException ex = new CustomException("CODE", "message");

        assertThat(ex.getCode()).isEqualTo("CODE");
        assertThat(ex.getMessage()).isEqualTo("message");
        assertThat(ex.getHttpStatusCode()).isNull();
        assertThat(ex.getErrors()).isNull();
    }

    @Test
    void shouldBuildExceptionWithCodeMessageAndHttpStatus() {
        CustomException ex = new CustomException("CODE", "message", "400");

        assertThat(ex.getCode()).isEqualTo("CODE");
        assertThat(ex.getMessage()).isEqualTo("message");
        assertThat(ex.getHttpStatusCode()).isEqualTo("400");
        assertThat(ex.getErrors()).isNull();
    }

    @Test
    void shouldBuildExceptionFromErrorsMap() {
        Map<String, String> errors = Map.of("field", "is invalid");

        CustomException ex = new CustomException(errors);

        assertThat(ex.getErrors()).isEqualTo(errors);
        assertThat(ex.getMessage()).isEqualTo(errors.toString());
        assertThat(ex.getCode()).isNull();
        assertThat(ex.getHttpStatusCode()).isNull();
    }
}
