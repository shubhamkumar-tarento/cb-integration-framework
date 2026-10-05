package com.igot.cb.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiCallDataCacheTest {

    @Test
    void shouldInstantiateComponent() {
        ApiCallDataCache cache = new ApiCallDataCache();
        assertThat(cache).isNotNull();
    }
}
