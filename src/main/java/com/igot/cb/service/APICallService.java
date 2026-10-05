package com.igot.cb.service;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import reactor.core.publisher.Mono;

public interface APICallService {

    Mono<ResponseDTO> makeExternalApiCall(ExternalApiIntegrationDTO<?> externalApiIntegrationDTO);
}
