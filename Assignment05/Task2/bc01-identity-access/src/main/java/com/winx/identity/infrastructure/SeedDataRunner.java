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
        UserAccount marianne = registrationService.registerUser(
                "Marianne Chen", "marianne@instant-mobility.example", "password123",
                "+491111111", LocalDate.of(1994, 7, 19));
        log.info("Seeded user 'Marianne Chen' with id={}", marianne.getId());

        UserAccount rowena = registrationService.registerUser(
                "Rowena Smith", "rowena@instant-mobility.example", "password123",
                "+492222222", LocalDate.of(1990, 3, 25));
        log.info("Seeded user 'Rowena Smith' with id={}", rowena.getId());

        ProviderAccount samaProvider = registrationService.registerProvider(
                "Sama Rides", "Sama Operator", "sama@providers.instant-mobility.example",
                "password123", "+493333333");
        log.info("Seeded provider 'Sama Rides' with id={}", samaProvider.getId());

        ProviderAccount maeProvider = registrationService.registerProvider(
                "Mae Urban Mobility", "Mae Manager", "mae@providers.instant-mobility.example",
                "password123", "+494444444");
        log.info("Seeded provider 'Mae Urban Mobility' with id={}", maeProvider.getId());
    }
}
