package com.igot.cb.controller;


import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/integration")
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping("/v1/create-external-call")
    public Mono<ResponseDTO> createExternalAPICall(@RequestBody ExternalApiIntegrationDTO<?> integrationDTO) {
        try {
            return integrationService.createExternalAPICall(integrationDTO);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @GetMapping("/v1/health")
    public String healthCheck() {
        return "Success";
    }

}
