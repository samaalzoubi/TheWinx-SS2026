package com.winx.booking.api.dto;

/** Request body sent to Fleet Management's {@code PATCH /vehicles/{id}/status}. */
public record StatusUpdate(String status) {
}
