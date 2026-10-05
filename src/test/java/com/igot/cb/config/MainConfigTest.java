package com.igot.cb.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class MainConfigTest {

    private final MainConfig mainConfig = new MainConfig();

    @Test
    void shouldCreateObjectMapperWithUnknownPropertiesDisabled() {
        ObjectMapper objectMapper = mainConfig.objectMapper();

        assertThat(objectMapper.getDeserializationConfig()
                .isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }

    @Test
    void shouldCreateJacksonConverterUsingGivenObjectMapper() {
        ObjectMapper objectMapper = mainConfig.objectMapper();

        MappingJackson2HttpMessageConverter converter = mainConfig.jacksonConverter(objectMapper);

        assertThat(converter.getObjectMapper()).isSameAs(objectMapper);
    }
}
