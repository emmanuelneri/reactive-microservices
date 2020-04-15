package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.mapper.JsonConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.CustomerSchema;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.KafkaContainer;

import java.time.LocalDateTime;

@RunWith(VertxUnitRunner.class)
public class ScheduleConsumerVerticleIT {

    @Rule
    public KafkaContainer kafka = new KafkaContainer("5.2.1");
    private JsonObject configuration;
    private Vertx vertx;

    @Before
    public void before() {
        configuration = new JsonObject()
                .put("kafka.bootstrap.servers", kafka.getBootstrapServers())
                .put("kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.offset.reset", "earliest")
                .put("kafka.enable.auto.commit", "false");

        this.vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void shouldConsumeMessage(final TestContext context) {
        final CustomerSchema customerSchema = new CustomerSchema();
        customerSchema.setDocumentNumber("948948393849");
        customerSchema.setName("Customer 1");
        customerSchema.setPhone("4499099493");

        final ScheduleSchema schema = new ScheduleSchema();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().plusDays(1));
        schema.setDescription("Complete Test");

        produceMessage(customerSchema.getDocumentNumber(), Json.encode(schema));

        final KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        final Async async = context.async();
        this.vertx.deployVerticle(new ScheduleConsumerVerticle(kafkaConsumerConfiguration));
        vertx.eventBus().<JsonObject>consumer(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), message -> {
            final Schedule schedule = message.body().mapTo(Schedule.class);
            Assert.assertNotNull(schedule);
            Assert.assertEquals("Complete Test", schedule.getDescription());
            message.reply("ok");
            async.complete();
        });
    }

    @Test
    public void shouldNotReciveMessageWithFormatErrorAndCommitMessages(final TestContext context) {
        produceMessage("123", "teste");
        produceMessage("4456", "{\"dateTime\":\"12-04-2020\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}");

        final KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        final Async async = context.async(2);
        this.vertx.deployVerticle(new ScheduleConsumerVerticle(kafkaConsumerConfiguration));

        vertx.eventBus().<JsonObject>consumer(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), message -> {
            context.fail("Error messages should not be received");
        });

        final int producedMessage = 2;
        vertx.eventBus().<JsonObject>consumer(ScheduleCommandEvents.INVALID_SCHEDULE_RECEIVED.getName(), message -> {
            if (async.count() == producedMessage) {
                async.complete();
            }
        });
    }

    private void produceMessage(final String key, final String value) {
        final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(this.configuration);
        final KafkaProducer<String, String> producer = KafkaProducer.create(this.vertx, kafkaProducerConfiguration.createConfig());

        final KafkaProducerRecord<String, String> producerRecord = KafkaProducerRecord.create(
                ScheduleConsumerVerticle.SCHEDULE_REQUEST_TOPIC, key, value);

        producer.send(producerRecord);
    }
}