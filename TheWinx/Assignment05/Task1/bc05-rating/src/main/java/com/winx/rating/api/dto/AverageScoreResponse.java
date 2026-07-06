package com.winx.rating.api.dto;

public record AverageScoreResponse(Long vehicleId, double averageScore, int count) {
}
