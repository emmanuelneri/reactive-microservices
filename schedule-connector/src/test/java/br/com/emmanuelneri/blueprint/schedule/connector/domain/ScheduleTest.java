package br.com.emmanuelneri.blueprint.schedule.connector.domain;

import br.com.emmanuelneri.blueprint.exception.ValidationException;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class ScheduleTest {

    @Test(expected = ValidationException.class)
    public void shouldRetunValidationExpectionWithEmptySchedule() {
        final Schedule schedule = new Schedule();
        schedule.validate();
    }

    @Test
    public void shouldNotReturnExceptionWithAllFieldsFilled() {
        final Schedule schedule = new Schedule();
        schedule.setDateTime(LocalDateTime.now().plusDays(1));
        schedule.setDescription("Schedule");

        final Customer customer = new Customer();
        customer.setDocumentNumber("32423423423523");
        customer.setName("Customer");
        customer.setPhone("4494834390493");
        customer.setEmail("customer@gmail.com");
        schedule.setCustomer(customer);

        schedule.validate();
    }

    @Test(expected = ValidationException.class)
    public void shoulReturnExceptionWithPastDateTime() {
        final Schedule schedule = new Schedule();
        schedule.setDateTime(LocalDateTime.now().minusDays(1));
        schedule.setDescription("Schedule");

        final Customer customer = new Customer();
        customer.setDocumentNumber("32423423423523");
        customer.setName("Customer");
        customer.setPhone("4494834390493");
        customer.setEmail("customer@gmail.com");
        schedule.setCustomer(customer);

        schedule.validate();
    }
}