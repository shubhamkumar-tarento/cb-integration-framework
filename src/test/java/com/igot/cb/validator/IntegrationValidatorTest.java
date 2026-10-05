package com.igot.cb.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationValidatorTest {

    @Mock
    private ObjectMapper objectMapper;

    private IntegrationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IntegrationValidator(objectMapper);
    }

    private ExternalApiIntegrationDTO.ExternalApiIntegrationDTOBuilder<Object> validBuilder() {
        return ExternalApiIntegrationDTO.builder()
                .serviceName("service")
                .serviceCode("code")
                .url("http://localhost")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestHeader(Map.of("content-type", "application/json"))
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .requestBody("{}");
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("MISSING_REQUEST");
    }

    @Test
    void shouldThrowWhenServiceNameMissing() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder().serviceName(" ").build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("SERVICE_NAME");
    }

    @Test
    void shouldThrowWhenServiceCodeMissing() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder().serviceCode(" ").build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("SERVICE_CODE");
    }

    @Test
    void shouldThrowWhenUrlMissing() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder().url(" ").build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("REQUEST_URL");
    }

    @Test
    void shouldThrowWhenRequestMethodMissing() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder().requestMethod(null).build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("REQUEST_METHOD");
    }

    @Test
    void shouldThrowWhenRequestHeadersMissing() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder().requestHeader(Map.of()).build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("REQUEST_HEADERS");
    }

    @Test
    void shouldThrowWhenOperationTypeMissing() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder().operationType(null).build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("OPERATION_TYPE");
    }

    @Test
    void shouldThrowWhenRequestBodyMissingForNonGetMethod() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder().requestBody(null).build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("MISSING_REQUEST_BODY");
    }

    @Test
    void shouldNotThrowWhenRequestBodyMissingForGetMethod() {
        ExternalApiIntegrationDTO<Object> dto = validBuilder()
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.GET)
                .requestBody(null)
                .build();

        assertThatCode(() -> validator.validate(dto)).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenRequestBodyIsInvalidJson() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonParseException((com.fasterxml.jackson.core.JsonParser) null, "bad json"));
        ExternalApiIntegrationDTO<Object> dto = validBuilder().build();

        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST_BODY");
    }

    @Test
    void shouldPassValidationForValidRequest() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        ExternalApiIntegrationDTO<Object> dto = validBuilder().build();

        assertThatCode(() -> validator.validate(dto)).doesNotThrowAnyException();
    }
}
