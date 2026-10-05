package com.igot.cb.service.impl;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.service.EnrichmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnrichmentServiceImpl implements EnrichmentService {

    @Override
    public void enrich(ExternalApiIntegrationDTO<?> integrationDTO) {
        enrichDefaultHeader(integrationDTO);
    }

    private void enrichDefaultHeader(ExternalApiIntegrationDTO<?> integrationDTO) {
        log.debug("EnrichmentServiceImpl::enrichDefaultHeader::{}", integrationDTO);
    }
}
