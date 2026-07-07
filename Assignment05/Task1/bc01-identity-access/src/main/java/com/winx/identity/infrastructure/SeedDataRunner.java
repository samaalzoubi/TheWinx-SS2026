package com.winx.identity.infrastructure;

import com.winx.identity.domain.ProviderAccount;
import com.winx.identity.domain.UserAccount;
import com.winx.identity.domain.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// we seed a few accounts on startup so the service isn't empty when demoing or running integration tests
@Component
public class SeedDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);

    private final RegistrationService registrationService;

    public SeedDataRunner(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public void run(String... args) {
        UserAccount alice = registrationService.registerUser(
                "Alice Example", "alice@example.com", "password123",
                "+491111111", LocalDate.of(1992, 3, 14));
        log.info("Seeded user 'Alice Example' with id={}", alice.getId());

        UserAccount bob = registrationService.registerUser(
                "Bob Example", "bob@example.com", "password123",
                "+492222222", LocalDate.of(1988, 11, 2));
        log.info("Seeded user 'Bob Example' with id={}", bob.getId());

        ProviderAccount greenWheels = registrationService.registerProvider(
                "GreenWheels GmbH", "Petra Provider", "petra@greenwheels.example",
                "password123", "+493333333");
        log.info("Seeded provider 'GreenWheels GmbH' with id={}", greenWheels.getId());

        ProviderAccount urbanRide = registrationService.registerProvider(
                "UrbanRide Inc", "Sam Fleet", "sam@urbanride.example",
                "password123", "+494444444");
        log.info("Seeded provider 'UrbanRide Inc' with id={}", urbanRide.getId());
    }
}
