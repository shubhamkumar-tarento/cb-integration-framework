package com.igot.cb.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDTOTest {

    @Test
    void shouldBuildAndExposeFields() {
        ResponseDTO responseDTO = ResponseDTO.builder()
                .id("id-1")
                .responseData("payload")
                .build();

        assertThat(responseDTO.getId()).isEqualTo("id-1");
        assertThat(responseDTO.getResponseData()).isEqualTo("payload");
    }

    @Test
    void shouldSupportNoArgsConstructorAndSetters() {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setId("id-2");
        responseDTO.setResponseData("data");

        assertThat(responseDTO.getId()).isEqualTo("id-2");
        assertThat(responseDTO.getResponseData()).isEqualTo("data");
    }
}
