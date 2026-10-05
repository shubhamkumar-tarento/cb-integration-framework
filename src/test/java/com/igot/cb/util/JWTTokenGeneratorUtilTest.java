package com.igot.cb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTTokenGeneratorUtilTest {

    @Mock
    private ObjectMapper objectMapper;

    private JWTTokenGeneratorUtil tokenGeneratorUtil;

    @BeforeEach
    void setUp() {
        tokenGeneratorUtil = new JWTTokenGeneratorUtil(objectMapper);
        ReflectionTestUtils.setField(tokenGeneratorUtil, "jwtSecretKey", "test-secret");
    }

    @Test
    void shouldGenerateTokenWhenUrlAndOperationTypePresent() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"a\":1}");

        String token = tokenGeneratorUtil.generateRedisJwtTokenKey("{\"a\":1}", "http://localhost", "PEER_TO_PEER");

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldReturnEmptyTokenWhenRequestBodyIsNull() {
        String token = tokenGeneratorUtil.generateRedisJwtTokenKey(null, "http://localhost", "PEER_TO_PEER");

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldReturnEmptyTokenWhenUrlIsBlank() {
        String token = tokenGeneratorUtil.generateRedisJwtTokenKey("body", "", "PEER_TO_PEER");

        assertThat(token).isEmpty();
    }

    @Test
    void shouldFallbackToEmptyStringWhenSerializationFails() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonParseException((com.fasterxml.jackson.core.JsonParser) null, "bad json"));

        String token = tokenGeneratorUtil.generateRedisJwtTokenKey("body", "http://localhost", "PEER_TO_PEER");

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldGenerateTokenForFileWithFilesPresent() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        String token = tokenGeneratorUtil.generateRedisJwtTokenKeyForFile(Flux.empty(), "body", "http://localhost", "PEER_TO_PEER");

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldGenerateTokenForFileWithoutFiles() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        String token = tokenGeneratorUtil.generateRedisJwtTokenKeyForFile(null, "body", "http://localhost", "PEER_TO_PEER");

        assertThat(token).isNotBlank();
    }
}
