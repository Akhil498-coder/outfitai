package com.outfitai.health;

import java.time.Instant;

/** DTO returned by GET /api/health. A record is an immutable data carrier (no boilerplate). */
public record HealthResponse(String status, String db, Instant time) {
}
