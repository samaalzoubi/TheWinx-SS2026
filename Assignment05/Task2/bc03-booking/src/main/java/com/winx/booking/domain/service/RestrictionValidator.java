package com.winx.booking.domain.service;

import com.winx.booking.domain.exception.RestrictionViolationException;
import com.winx.booking.infrastructure.UserView;
import com.winx.booking.infrastructure.VehicleView;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

// we only check minimum age here, maxPersons/maxDurationMinutes/maxKilometers aren't part of the booking creation inputs in this iteration
@Service
public class RestrictionValidator {

    public void validate(UserView user, VehicleView vehicle) {
        if (vehicle.minAge() != null) {
            int age = Period.between(user.dateOfBirth(), LocalDate.now()).getYears();
            if (age < vehicle.minAge()) {
                throw new RestrictionViolationException(
                        "User " + user.id() + " (age " + age + ") does not meet the minimum age requirement ("
                                + vehicle.minAge() + ") for vehicle " + vehicle.id());
            }
        }
    }
}
