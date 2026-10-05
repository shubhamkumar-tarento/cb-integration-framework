package com.igot.cb.service;

import com.igot.cb.model.ExternalApiIntegrationDTO;

public interface EnrichmentService {

    void enrich(ExternalApiIntegrationDTO<?> integrationDTO);
}
