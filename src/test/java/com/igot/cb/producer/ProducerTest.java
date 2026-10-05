package com.igot.cb.producer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void shouldSendMessageToKafkaTemplate() {
        Producer producer = new Producer(kafkaTemplate);

        producer.send("test-topic", "test-message");

        verify(kafkaTemplate).send("test-topic", "test-message");
    }
}
