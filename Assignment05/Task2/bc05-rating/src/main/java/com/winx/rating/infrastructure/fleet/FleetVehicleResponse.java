package com.winx.rating.infrastructure.fleet;

/**
 * Partial projection of bc02-fleet-management's vehicle representation - only
 * the fields Rating needs to resolve a stable Provider reference for a
 * booking's vehicle.
 */
public record FleetVehicleResponse(Long id, Long providerId) {
}
