package com.currencyconverter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record RateSnapshot(Map<String, BigDecimal> rates, Instant updatedAt, Instant fetchedAt) {
    public RateSnapshot { rates = Map.copyOf(rates); }
}
