package com.igot.cb.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalApiIntegrationDTOTest {

    @Test
    void shouldBuildAndExposeFields() {
        ExternalApiIntegrationDTO<String> dto = ExternalApiIntegrationDTO.<String>builder()
                .url("http://localhost")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestHeader(Map.of("content-type", "application/json"))
                .requestBody("{}")
                .responseClassType("type")
                .serviceCode("code")
                .serviceName("name")
                .serviceDescription("desc")
                .responseData("resp")
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .id("id-1")
                .strictCache(true)
                .strictCacheTimeInMinutes(5)
                .alwaysDataReadFromCache(true)
                .isFormData(true)
                .build();

        assertThat(dto.getUrl()).isEqualTo("http://localhost");
        assertThat(dto.getRequestMethod()).isEqualTo(ExternalApiIntegrationDTO.RequestMethod.POST);
        assertThat(dto.getRequestHeader()).containsEntry("content-type", "application/json");
        assertThat(dto.getRequestBody()).isEqualTo("{}");
        assertThat(dto.getResponseClassType()).isEqualTo("type");
        assertThat(dto.getServiceCode()).isEqualTo("code");
        assertThat(dto.getServiceName()).isEqualTo("name");
        assertThat(dto.getServiceDescription()).isEqualTo("desc");
        assertThat(dto.getResponseData()).isEqualTo("resp");
        assertThat(dto.getOperationType()).isEqualTo(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        assertThat(dto.getId()).isEqualTo("id-1");
        assertThat(dto.isStrictCache()).isTrue();
        assertThat(dto.getStrictCacheTimeInMinutes()).isEqualTo(5);
        assertThat(dto.isAlwaysDataReadFromCache()).isTrue();
        assertThat(dto.isFormData()).isTrue();
    }

    @Test
    void shouldSupportNoArgsConstructorAndSetters() {
        ExternalApiIntegrationDTO<Object> dto = new ExternalApiIntegrationDTO<>();
        dto.setId("id-2");
        dto.setUrl("http://example.com");

        assertThat(dto.getId()).isEqualTo("id-2");
        assertThat(dto.getUrl()).isEqualTo("http://example.com");
    }

    @Test
    void shouldResolveOperationTypeFromValue() {
        assertThat(ExternalApiIntegrationDTO.OperationType.fromValue("PEER_TO_PEER"))
                .isEqualTo(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        assertThat(ExternalApiIntegrationDTO.OperationType.fromValue("FIRE_AND_FORGET"))
                .isEqualTo(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);
        assertThat(ExternalApiIntegrationDTO.OperationType.fromValue("UNKNOWN")).isNull();
        assertThat(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER.toString()).isEqualTo("PEER_TO_PEER");
    }

    @Test
    void shouldResolveRequestMethodFromValue() {
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("GET"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.GET);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("POST"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.POST);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("PUT"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.PUT);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("PATCH"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.PATCH);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("DELETE"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.DELETE);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("OPTIONS"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.OPTIONS);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("TRACE"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.TRACE);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("HEAD"))
                .isEqualTo(ExternalApiIntegrationDTO.RequestMethod.HEAD);
        assertThat(ExternalApiIntegrationDTO.RequestMethod.fromValue("UNKNOWN")).isNull();
        assertThat(ExternalApiIntegrationDTO.RequestMethod.GET.toString()).isEqualTo("GET");
    }
}
