package com.currencyconverter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {
    private final RatesClient client;
    private final Clock clock;
    private RateSnapshot cached;
    private Instant retryAfter = Instant.MIN;
    public ExchangeRateService(RatesClient client, Clock clock) { this.client = client; this.clock = clock; }
    public record RatesResult(String baseCurrency, Map<String, BigDecimal> rates, Instant updatedAt, boolean stale) {}
    public record ConversionResult(BigDecimal amount, String fromCurrency, String toCurrency, BigDecimal convertedAmount,
                                   BigDecimal rate, Instant updatedAt, boolean stale) {}
    public static class RatesUnavailableException extends RuntimeException {
        public RatesUnavailableException() { super("Exchange rates are temporarily unavailable. Please try again shortly."); }
    }
    private boolean usable(RateSnapshot snapshot, Instant now) {
        return snapshot != null && !snapshot.updatedAt().isBefore(now.minus(Duration.ofHours(48)));
    }
    private synchronized RateSnapshot snapshot() {
        Instant now = clock.instant();
        if (usable(cached, now) && now.isBefore(cached.fetchedAt().plus(Duration.ofHours(1)))) return cached;
        if (now.isBefore(retryAfter)) {
            if (usable(cached, now)) return cached;
            throw new RatesUnavailableException();
        }
        try {
            RateSnapshot latest = client.fetch();
            if (!usable(latest, now)) throw new IllegalStateException("Provider rates are too old");
            cached = latest;
            retryAfter = Instant.MIN;
        } catch (RuntimeException error) {
            retryAfter = now.plusSeconds(60);
            if (!usable(cached, now)) throw new RatesUnavailableException();
        }
        return cached;
    }
    private boolean stale(RateSnapshot snapshot) {
        Instant now = clock.instant();
        return now.isBefore(retryAfter) || snapshot.updatedAt().isBefore(now.minus(Duration.ofHours(26)));
    }
    private BigDecimal crossRate(RateSnapshot snapshot, String from, String to) {
        return snapshot.rates().get(to).divide(snapshot.rates().get(from), MathContext.DECIMAL128);
    }
    public RatesResult rates(String baseCurrency) {
        String base = SupportedCurrency.require(baseCurrency);
        RateSnapshot snapshot = snapshot();
        var result = new LinkedHashMap<String, BigDecimal>();
        SupportedCurrency.ALL.forEach(currency -> result.put(currency.code(), crossRate(snapshot, base, currency.code()).setScale(10, RoundingMode.HALF_UP)));
        return new RatesResult(base, result, snapshot.updatedAt(), stale(snapshot));
    }
    public ConversionResult convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null || amount.signum() < 0 || amount.compareTo(new BigDecimal("1000000000000")) > 0 || amount.scale() > 6) {
            throw new IllegalArgumentException("Enter an amount between 0 and 1,000,000,000,000 with at most 6 decimal places.");
        }
        String from = SupportedCurrency.require(fromCurrency), to = SupportedCurrency.require(toCurrency);
        RateSnapshot snapshot = snapshot();
        BigDecimal rate = crossRate(snapshot, from, to);
        int decimals = Currency.getInstance(to).getDefaultFractionDigits();
        BigDecimal converted = amount.multiply(rate, MathContext.DECIMAL128).setScale(decimals, RoundingMode.HALF_UP);
        return new ConversionResult(amount, from, to, converted, rate.setScale(10, RoundingMode.HALF_UP), snapshot.updatedAt(), stale(snapshot));
    }
}
