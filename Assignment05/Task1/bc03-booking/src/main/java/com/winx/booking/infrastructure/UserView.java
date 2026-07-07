package com.winx.booking.infrastructure;

import java.time.LocalDate;

public record UserView(Long id, String name, LocalDate dateOfBirth) {
}
