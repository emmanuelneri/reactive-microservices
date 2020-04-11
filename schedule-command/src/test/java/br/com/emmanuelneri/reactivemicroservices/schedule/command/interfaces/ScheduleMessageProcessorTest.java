package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import io.vertx.core.Vertx;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

public class ScheduleMessageProcessorTest {

    @Test
    public void shouldNotReturnExceptionWithNoJsonMessageValue() {
        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(Vertx.vertx());
        final String messageValue = "teste";
        scheduleMessageProcessor.process(new ConsumerRecord<>("topic", 0, 0, "123", messageValue));
    }

    @Test
    public void shouldNotReturnExceptionWithInvalidJsonMessageValue() {
        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(Vertx.vertx());
        final String messageValue = "{\"dateTime\":\"12-04-2020\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";
        scheduleMessageProcessor.process(new ConsumerRecord<>("topic", 0, 0, "123", messageValue));
    }
}