package com.igot.cb.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
@Slf4j
public class IntegrationValidator {

    private final ObjectMapper objectMapper;

    public IntegrationValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validate(ExternalApiIntegrationDTO<?> integrationDTO) {
        log.info("IntegrationServiceImpl::validate");
        if (integrationDTO == null) {
            throw new CustomException("MISSING_REQUEST", "request is missing!");
        }

        validateMandatoryFields(integrationDTO);
        validateRequestBody(integrationDTO);
    }

    private void validateMandatoryFields(ExternalApiIntegrationDTO<?> integrationDTO) {
        if (StringUtils.isBlank(integrationDTO.getServiceName())) {
            throw new CustomException("SERVICE_NAME", "service name is missing in request!");
        }
        if (StringUtils.isBlank(integrationDTO.getServiceCode())) {
            throw new CustomException("SERVICE_CODE", "service code is missing in request!");
        }
        if (StringUtils.isBlank(integrationDTO.getUrl())) {
            throw new CustomException("REQUEST_URL", "url is missing in request");
        }
        if (integrationDTO.getRequestMethod() == null || !EnumUtils.isValidEnum(RequestMethod.class, integrationDTO.getRequestMethod().name())) {
            throw new CustomException("REQUEST_METHOD", "request method is missing in request!");
        }

        if (CollectionUtils.isEmpty(integrationDTO.getRequestHeader())) {
            throw new CustomException("REQUEST_HEADERS", "request headers are missing in request");
        }

        if (integrationDTO.getOperationType() == null || !EnumUtils.isValidEnum(ExternalApiIntegrationDTO.OperationType.class, integrationDTO.getOperationType().name())) {
            throw new CustomException("OPERATION_TYPE", "Operation type is not valid!");
        }
    }

    private void validateRequestBody(ExternalApiIntegrationDTO<?> integrationDTO) {
        if (integrationDTO.getRequestBody() == null
                && integrationDTO.getRequestMethod() != null
                && integrationDTO.getRequestMethod() != ExternalApiIntegrationDTO.RequestMethod.GET) {
            throw new CustomException("MISSING_REQUEST_BODY", "request body is missing in request");
        }

        if (integrationDTO.getRequestBody() != null && !isValidJson(integrationDTO.getRequestBody())) {
            throw new CustomException("INVALID_REQUEST_BODY", "request body is a invalid json");
        }
    }

    private boolean isValidJson(Object requestBody) {
        try {
            objectMapper.writeValueAsString(requestBody);
            return true;
        } catch (JsonProcessingException e) {
            log.debug("request body is not a valid json", e);
            return false;
        }
    }

}
