package br.com.emmanuelneri.reactivemicroservices.schedule.command.mapper;

import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.exceptions.InvalidScheduleSchemaException;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.CustomerSchema;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScheduleMapper {

    public static final ScheduleMapper INSTANCE = new ScheduleMapper();

    public void map(final ConsumerRecord<String, String> record, final Handler<AsyncResult<Schedule>> handler) {
        try {
            final ScheduleSchema schema = Json.decodeValue(record.value(), ScheduleSchema.class);

            final Schedule schedule = new Schedule();
            schedule.setDateTime(schema.getDateTime());
            schedule.setDescription(schema.getDescription());

            final CustomerSchema customer = schema.getCustomer();
            schedule.setCustomer(customer.getName());
            schedule.setDocumentNumber(customer.getDocumentNumber());
            schedule.setEmail(customer.getEmail());
            schedule.setPhone(customer.getPhone());

            handler.handle(Future.succeededFuture(schedule));
        } catch (final DecodeException | IllegalArgumentException ex) {
            handler.handle(Future.failedFuture(new InvalidScheduleSchemaException(ex.getMessage())));
        } catch (final Exception ex) {
            handler.handle(Future.failedFuture(ex));
        }
    }
}
