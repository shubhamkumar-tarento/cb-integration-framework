package com.igot.cb.service.impl;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.producer.Producer;
import com.igot.cb.service.APICallService;
import com.igot.cb.service.EnrichmentService;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import com.igot.cb.validator.IntegrationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceImplTest {

    @Mock
    private IntegrationValidator integrationValidator;
    @Mock
    private APICallService apiCallService;
    @Mock
    private Producer producer;
    @Mock
    private EnrichmentService enrichmentService;
    @Mock
    private JWTTokenGeneratorUtil tokenGeneratorUtil;
    @Mock
    private ReactiveRedisOperations<String, ResponseDTO> cacheOps;
    @Mock
    private ReactiveValueOperations<String, ResponseDTO> valueOperations;

    private IntegrationServiceImpl integrationService;

    @BeforeEach
    void setUp() {
        integrationService = new IntegrationServiceImpl(integrationValidator, apiCallService, producer,
                enrichmentService, tokenGeneratorUtil, cacheOps);
        ReflectionTestUtils.setField(integrationService, "callExternalServiceTopic", "test-topic");
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-1");
    }

    private ExternalApiIntegrationDTO<Object> baseDto(ExternalApiIntegrationDTO.OperationType operationType) {
        return ExternalApiIntegrationDTO.builder()
                .url("http://localhost")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestHeader(Map.of("content-type", "application/json"))
                .requestBody("{}")
                .operationType(operationType)
                .build();
    }

    @Test
    void shouldReturnGeneratedIdForFireAndForgetOperation() {
        ExternalApiIntegrationDTO<Object> dto = baseDto(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .assertNext(response -> assertThat(response.getId()).isNotBlank())
                .verifyComplete();

        verify(producer).send("test-topic", dto);
    }

    @Test
    void shouldThrowCustomExceptionWhenProducerFailsForFireAndForget() {
        ExternalApiIntegrationDTO<Object> dto = baseDto(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);
        doThrow(new RuntimeException("kafka down")).when(producer).send(anyString(), any());

        StepVerifier.create(Mono.defer(() -> integrationService.createExternalAPICall(dto)))
                .expectErrorMatches(error -> error.getMessage().contains("kafka down"))
                .verify();
    }

    @Test
    void shouldCallExternalApiWhenStrictCacheDisabled() {
        ExternalApiIntegrationDTO<Object> dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(false);
        ResponseDTO responseDTO = ResponseDTO.builder().id("resp-1").build();
        when(apiCallService.makeExternalApiCall(dto)).thenReturn(Mono.just(responseDTO));

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @Test
    void shouldReturnCachedDataWhenAlwaysReadFromCacheAndCacheHit() {
        ExternalApiIntegrationDTO<Object> dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(true);
        ResponseDTO cached = ResponseDTO.builder().id("cached").build();
        when(cacheOps.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token-1")).thenReturn(Mono.just(cached));

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(cached)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyResponseWhenAlwaysReadFromCacheAndCacheMiss() {
        ExternalApiIntegrationDTO<Object> dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(true);
        when(cacheOps.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token-1")).thenReturn(Mono.empty());

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .assertNext(response -> assertThat(response.getId()).isNull())
                .verifyComplete();
    }

    @Test
    void shouldReturnCachedDataWhenStrictCacheAndCacheHit() {
        ExternalApiIntegrationDTO<Object> dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(false);
        ResponseDTO cached = ResponseDTO.builder().id("cached").build();
        when(cacheOps.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token-1")).thenReturn(Mono.just(cached));
        when(apiCallService.makeExternalApiCall(dto)).thenReturn(Mono.empty());

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(cached)
                .verifyComplete();
    }

    @Test
    void shouldCallExternalApiWhenStrictCacheAndCacheMiss() {
        ExternalApiIntegrationDTO<Object> dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(false);
        ResponseDTO responseDTO = ResponseDTO.builder().id("resp-2").build();
        when(cacheOps.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token-1")).thenReturn(Mono.empty());
        when(apiCallService.makeExternalApiCall(dto)).thenReturn(Mono.just(responseDTO));

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(responseDTO)
                .verifyComplete();
    }
}
