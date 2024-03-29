package dev.lydtech.tracking.service;

import dev.lydtech.dispatch.message.DispatchCompleted;
import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.TrackingStatusUpdated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class TrackingServiceTest {

    private TrackingService service;
    private KafkaTemplate kafkaProducerMock;
    private DispatchPreparing testEvent = DispatchPreparing.builder().orderId(randomUUID()).build();
    private DispatchCompleted testEventCompleted =
            DispatchCompleted.builder().orderId(UUID.randomUUID()).date(Instant.now().toString()).build();

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock(KafkaTemplate.class);
        service = new TrackingService(kafkaProducerMock);
    }

    @Test
    void process_SuccessPrepare() throws Exception {
        when(kafkaProducerMock.send(anyString(), any(TrackingStatusUpdated.class))).thenReturn(
                mock(CompletableFuture.class));

        service.process(testEvent);

        verify(kafkaProducerMock, times(1)).send(eq("tracking.status"), any(TrackingStatusUpdated.class));
    }

    @Test
    void process_SuccessCompleted() throws Exception {
        when(kafkaProducerMock.send(anyString(), any(TrackingStatusUpdated.class))).thenReturn(
                mock(CompletableFuture.class));

        service.process(testEventCompleted);

        verify(kafkaProducerMock, times(1)).send(eq("tracking.status"), any(TrackingStatusUpdated.class));
    }

    @Test
    void process_Throws() {
        doThrow(new RuntimeException("Producer failure")).when(kafkaProducerMock).send(eq("tracking.status"),
                any(TrackingStatusUpdated.class));

        Exception exception = assertThrows(RuntimeException.class, () -> service.process(testEvent));

        verify(kafkaProducerMock, times(1)).send(eq("tracking.status"), any(TrackingStatusUpdated.class));
        assertThat(exception.getMessage(), equalTo("Producer failure"));
    }
    @Test
    void processCompleted_Throws() {
        doThrow(new RuntimeException("Producer failure")).when(kafkaProducerMock).send(eq("tracking.status"), any(TrackingStatusUpdated.class));

        Exception exception = assertThrows(RuntimeException.class, () -> service.process(testEventCompleted));

        verify(kafkaProducerMock, times(1)).send(eq("tracking.status"), any(TrackingStatusUpdated.class));
        assertThat(exception.getMessage(), equalTo("Producer failure"));
    }
}

