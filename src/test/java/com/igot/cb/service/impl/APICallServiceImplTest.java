package com.igot.cb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class APICallServiceImplTest {

    private static DisposableServer server;
    private static String baseUrl;

    @Mock
    private ReactiveRedisOperations<String, ResponseDTO> cacheOps;
    @Mock
    private ReactiveValueOperations<String, ResponseDTO> valueOperations;
    @Mock
    private JWTTokenGeneratorUtil tokenGeneratorUtil;

    private APICallServiceImpl apiCallService;

    @BeforeAll
    static void startServer() {
        server = HttpServer.create()
                .host("localhost")
                .port(0)
                .route(routes -> routes
                        .get("/get-ok", (request, response) -> response
                                .header("content-type", "application/json")
                                .sendString(Mono.just("{\"result\":\"ok\"}")))
                        .post("/post-ok", (request, response) -> request.receive().aggregate().asString()
                                .defaultIfEmpty("")
                                .flatMap(body -> response.header("content-type", "application/json")
                                        .sendString(Mono.just("{\"echo\":\"ok\"}"))
                                        .then()))
                        .post("/post-nonjson", (request, response) -> request.receive().aggregate().asString()
                                .defaultIfEmpty("")
                                .flatMap(body -> response.sendString(Mono.just("plain-text-response")).then()))
                        .post("/form", (request, response) -> request.receive().aggregate().asString()
                                .defaultIfEmpty("")
                                .flatMap(body -> response.header("content-type", "application/json")
                                        .sendString(Mono.just("{\"form\":\"ok\"}"))
                                        .then()))
                        .get("/error", (request, response) -> response.status(500)
                                .sendString(Mono.just("server error")).then()))
                .bindNow();
        baseUrl = "http://localhost:" + server.port();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @BeforeEach
    void setUp() {
        apiCallService = new APICallServiceImpl(cacheOps, tokenGeneratorUtil, new ObjectMapper());
        ReflectionTestUtils.setField(apiCallService, "cacheDataTtl", 60000L);
        ReflectionTestUtils.setField(apiCallService, "maxResponseMemorySize", 16777216);
        lenient().when(cacheOps.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.set(anyString(), any(), any(Duration.class))).thenReturn(Mono.just(true));
        lenient().when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-1");
    }

    private ExternalApiIntegrationDTO<Object> dto(ExternalApiIntegrationDTO.RequestMethod method, String path, Object body, boolean formData) {
        return ExternalApiIntegrationDTO.builder()
                .url(baseUrl + path)
                .requestMethod(method)
                .requestHeader(Map.of("content-type", "application/json"))
                .requestBody(body)
                .isFormData(formData)
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .strictCacheTimeInMinutes(0)
                .build();
    }

    @Test
    void shouldReturnJsonResponseForGetRequest() {
        ExternalApiIntegrationDTO<Object> request = dto(ExternalApiIntegrationDTO.RequestMethod.GET, "/get-ok", null, false);

        StepVerifier.create(apiCallService.makeExternalApiCall(request))
                .assertNext(response -> assertThat(response.getResponseData().toString()).contains("ok"))
                .verifyComplete();
    }

    @Test
    void shouldReturnParsedJsonResponseForPostRequest() {
        ExternalApiIntegrationDTO<Object> request = dto(ExternalApiIntegrationDTO.RequestMethod.POST, "/post-ok", "{\"a\":1}", false);

        StepVerifier.create(apiCallService.makeExternalApiCall(request))
                .assertNext(response -> assertThat(response.getResponseData().toString()).contains("echo"))
                .verifyComplete();
    }

    @Test
    void shouldFallBackToRawStringWhenResponseIsNotJson() {
        ExternalApiIntegrationDTO<Object> request = dto(ExternalApiIntegrationDTO.RequestMethod.POST, "/post-nonjson", "{\"a\":1}", false);

        StepVerifier.create(apiCallService.makeExternalApiCall(request))
                .assertNext(response -> assertThat(response.getResponseData()).isEqualTo("plain-text-response"))
                .verifyComplete();
    }

    @Test
    void shouldSendFormDataWhenFlagIsSet() {
        ExternalApiIntegrationDTO<Object> request = dto(ExternalApiIntegrationDTO.RequestMethod.POST, "/form",
                Map.of("field1", "value1", "field2", "value2"), true);

        StepVerifier.create(apiCallService.makeExternalApiCall(request))
                .assertNext(response -> assertThat(response.getResponseData().toString()).contains("form"))
                .verifyComplete();
    }

    @Test
    void shouldWrapErrorResponseInCustomException() {
        ExternalApiIntegrationDTO<Object> request = dto(ExternalApiIntegrationDTO.RequestMethod.GET, "/error", null, false);

        StepVerifier.create(apiCallService.makeExternalApiCall(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(CustomException.class);
                    CustomException customException = (CustomException) error;
                    assertThat(customException.getCode()).isEqualTo("EXTERNAL_SERVICE_CALL_ERROR");
                    assertThat(customException.getHttpStatusCode()).isEqualTo("500");
                })
                .verify();
    }

    @Test
    void shouldCacheWithUserSuppliedStrictCacheTime() {
        ExternalApiIntegrationDTO<Object> request = dto(ExternalApiIntegrationDTO.RequestMethod.GET, "/get-ok", null, false);
        request.setStrictCacheTimeInMinutes(5);
        ResponseDTO responseDTO = ResponseDTO.builder().id("id-1").build();

        StepVerifier.create(apiCallService.saveToRedis(request, "token-1", responseDTO))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldCacheWithConfiguredTtlWhenStrictCacheTimeNotSet() {
        ExternalApiIntegrationDTO<Object> request = dto(ExternalApiIntegrationDTO.RequestMethod.GET, "/get-ok", null, false);
        ResponseDTO responseDTO = ResponseDTO.builder().id("id-2").build();

        StepVerifier.create(apiCallService.saveToRedis(request, "token-1", responseDTO))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldConvertMapToMultiValueMap() {
        Map<String, Object> input = Map.of("key1", "value1", "key2", 2);

        var result = apiCallService.convertToStringMap(input);

        assertThat(result.getFirst("key1")).isEqualTo("value1");
        assertThat(result.getFirst("key2")).isEqualTo("2");
    }
}
