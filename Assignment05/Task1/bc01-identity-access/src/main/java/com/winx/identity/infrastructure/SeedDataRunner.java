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
                "Marianne Nosseir", "marianne@instant-mobility.example", "password123",
                "+491111111", LocalDate.of(1994, 7, 19));
        log.info("Seeded user 'Marianne Nosseir' with id={}", marianne.getId());

        UserAccount rowena = registrationService.registerUser(
                "Rowena Pagayanan", "rowena@instant-mobility.example", "password123",
                "+492222222", LocalDate.of(1990, 3, 25));
        log.info("Seeded user 'Rowena Pagayanan' with id={}", rowena.getId());

        UserAccount priyanka = registrationService.registerUser(
                "Priyanka Gupta", "priyanka@instant-mobility.example", "password123",
                "+495555555", LocalDate.of(1998, 11, 8));
        log.info("Seeded user 'Priyanka Gupta' with id={}", priyanka.getId());

        ProviderAccount samaProvider = registrationService.registerProvider(
                "Sama Mobility", "Sama Alzoubi", "sama@providers.instant-mobility.example",
                "password123", "+493333333");
        log.info("Seeded provider 'Sama Mobility' with id={}", samaProvider.getId());

        ProviderAccount maeProvider = registrationService.registerProvider(
                "Mae Urban Mobility", "Mae Eskandari Borujerdi", "mae@providers.instant-mobility.example",
                "password123", "+494444444");
        log.info("Seeded provider 'Mae Urban Mobility' with id={}", maeProvider.getId());
    }
}
