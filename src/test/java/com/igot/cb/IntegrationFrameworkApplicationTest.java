package com.igot.cb;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class IntegrationFrameworkApplicationTest {

    @Test
    void shouldDelegateToSpringApplicationRun() {
        String[] args = {"--server.port=0"};
        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            IntegrationFrameworkApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(IntegrationFrameworkApplication.class, args));
        }
    }
}