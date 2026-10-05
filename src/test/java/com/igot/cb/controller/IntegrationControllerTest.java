package com.igot.cb.controller;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.service.IntegrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationControllerTest {

    @Mock
    private IntegrationService integrationService;

    @Test
    void shouldReturnResponseFromService() {
        IntegrationController controller = new IntegrationController(integrationService);
        ResponseDTO responseDTO = ResponseDTO.builder().id("id-1").build();
        when(integrationService.createExternalAPICall(any())).thenReturn(Mono.just(responseDTO));

        ExternalApiIntegrationDTO<?> dto = ExternalApiIntegrationDTO.builder().url("http://localhost").build();

        StepVerifier.create(controller.createExternalAPICall(dto))
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @Test
    void shouldReturnMonoErrorWhenServiceThrows() {
        IntegrationController controller = new IntegrationController(integrationService);
        RuntimeException failure = new RuntimeException("boom");
        when(integrationService.createExternalAPICall(any())).thenThrow(failure);

        ExternalApiIntegrationDTO<?> dto = ExternalApiIntegrationDTO.builder().url("http://localhost").build();

        StepVerifier.create(controller.createExternalAPICall(dto))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    @Test
    void shouldReturnSuccessOnHealthCheck() {
        IntegrationController controller = new IntegrationController(integrationService);

        assertThat(controller.healthCheck()).isEqualTo("Success");
    }
}
