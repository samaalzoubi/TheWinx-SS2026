package com.winx.booking.domain.model;

import java.time.LocalDateTime;

// we kept this as a plain record too, Booking stores startTime/endTime as plain fields since endTime has to stay nullable until the ride finishes
public record TimeInterval(LocalDateTime start, LocalDateTime end) {
}
