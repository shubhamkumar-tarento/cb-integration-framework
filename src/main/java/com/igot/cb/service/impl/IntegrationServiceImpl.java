package com.igot.cb.service.impl;

import com.fasterxml.uuid.Generators;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.producer.Producer;
import com.igot.cb.service.APICallService;
import com.igot.cb.service.EnrichmentService;
import com.igot.cb.service.IntegrationService;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import com.igot.cb.validator.IntegrationValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class IntegrationServiceImpl implements IntegrationService {

    private final IntegrationValidator integrationValidator;
    private final APICallService apiCallService;
    private final Producer producer;
    private final EnrichmentService enrichmentService;
    private final JWTTokenGeneratorUtil tokenGeneratorUtil;
    private final ReactiveRedisOperations<String, ResponseDTO> cacheOps;

    @Value("${integration.kafka.create.topic}")
    private String callExternalServiceTopic;

    public IntegrationServiceImpl(IntegrationValidator integrationValidator,
                                   APICallService apiCallService,
                                   Producer producer,
                                   EnrichmentService enrichmentService,
                                   JWTTokenGeneratorUtil tokenGeneratorUtil,
                                   ReactiveRedisOperations<String, ResponseDTO> cacheOps) {
        this.integrationValidator = integrationValidator;
        this.apiCallService = apiCallService;
        this.producer = producer;
        this.enrichmentService = enrichmentService;
        this.tokenGeneratorUtil = tokenGeneratorUtil;
        this.cacheOps = cacheOps;
    }

    @Override
    public Mono<ResponseDTO> createExternalAPICall(ExternalApiIntegrationDTO<?> integrationDTO) {
        log.info("IntegrationServiceImpl::createExternalAPICall");


        integrationValidator.validate(integrationDTO);
        enrichmentService.enrich(integrationDTO);

        String token = tokenGeneratorUtil.generateRedisJwtTokenKey(integrationDTO.getRequestBody()
                , integrationDTO.getUrl()
                , integrationDTO.getOperationType().name());
        if (integrationDTO.getOperationType() == ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET) {
            try {
                UUID uuid = Generators.timeBasedGenerator().generate();
                String id = uuid.toString();
                integrationDTO.setId(id);

                producer.send(callExternalServiceTopic, integrationDTO);

                ResponseDTO responseDTO = new ResponseDTO();
                responseDTO.setId(id);
                return Mono.just(responseDTO);
            } catch (Exception e) {
                throw new CustomException("ERROR_IN_KAFKA_PRODUCER", e.getMessage());
            }

        } else {
            if (!integrationDTO.isStrictCache()) {//false
                log.info("Strict Cache set to false");
                log.info("Due cache false Data reading from externa api url: {}, calling external API", integrationDTO.getUrl());
                log.info("Due cache false Data reading from externa api token: {}, calling external API", token);
                return apiCallService.makeExternalApiCall(integrationDTO);
            } else {
                ResponseDTO responseDto = new ResponseDTO();
                if (integrationDTO.isAlwaysDataReadFromCache()) {
                    log.info("Always data reading from Cache");
                    return cacheOps.opsForValue().get(token)
                            .doOnNext(data -> {
                                log.info("Data reading from Redis for url: {}", integrationDTO.getUrl());
                                log.info("Data reading from Redis for token: {}", token);
                            })
                            .switchIfEmpty(Mono.just(responseDto)
                                    .doOnNext(data -> {
                                        log.info("Data not present in redis for the url {}", integrationDTO.getUrl());
                                        log.info("Data not present in redis for the token {}", token);
                                    }));

                } else {
                    return cacheOps.opsForValue().get(token)
                            .doOnNext(data -> {
                                log.info("Data reading from Redis for url: {}", integrationDTO.getUrl());
                                log.info("Data reading from Redis for token: {}", token);
                            })
                            .switchIfEmpty(
                                    apiCallService.makeExternalApiCall(integrationDTO)
                                            .doOnNext(data -> {
                                                log.info("Data not found in Redis for url: {}, calling external API", integrationDTO.getUrl());
                                                log.info("Data not found in Redis for token: {}, calling external API", token);
                                            })
                            );
                }

            }

        }
    }
}
