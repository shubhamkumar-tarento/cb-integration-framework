package com.igot.cb.service.impl;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EnrichmentServiceImplTest {

    private final EnrichmentServiceImpl enrichmentService = new EnrichmentServiceImpl();

    @Test
    void shouldEnrichWithoutThrowing() {
        ExternalApiIntegrationDTO<?> dto = ExternalApiIntegrationDTO.builder().url("http://localhost").build();

        assertThatCode(() -> enrichmentService.enrich(dto)).doesNotThrowAnyException();
    }
}
