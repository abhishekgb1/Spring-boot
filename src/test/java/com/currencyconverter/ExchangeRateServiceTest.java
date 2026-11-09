package com.currencyconverter;

import java.math.BigDecimal;
import java.time.*;
import java.util.Map;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTest {
    private final RatesClient client = mock(RatesClient.class);
    private final Clock clock = mock(Clock.class);
    private final Instant now = Instant.parse("2026-11-09T00:00:00Z");
    private ExchangeRateService service;
    @BeforeEach void setup() {
        when(clock.instant()).thenReturn(now);
        when(client.fetch()).thenReturn(new RateSnapshot(Map.of("USD",BigDecimal.ONE,"EUR",new BigDecimal("0.8"),"INR",new BigDecimal("80"),"JPY",new BigDecimal("150"),"GBP",new BigDecimal("0.7"),"CAD",new BigDecimal("1.4"),"AUD",new BigDecimal("1.5"),"CHF",new BigDecimal("0.9"),"CNY",new BigDecimal("7")),now,now));
        service = new ExchangeRateService(client,clock);
    }
    @Test void convertsCrossRatesWithDecimalArithmetic() {
        var result = service.convert(new BigDecimal("10.25"),"eur","inr");
        assertThat(result.convertedAmount()).isEqualByComparingTo("1025.00");
        assertThat(result.fromCurrency()).isEqualTo("EUR");
        assertThat(result.stale()).isFalse();
    }
    @Test void roundsToTargetCurrencyPrecision() {
        assertThat(service.convert(new BigDecimal("0.01"),"USD","JPY").convertedAmount()).isEqualByComparingTo("2");
        assertThat(service.convert(new BigDecimal("0.01"),"USD","EUR").convertedAmount()).isEqualByComparingTo("0.01");
    }
    @Test void zeroAndSameCurrencyAreSupported() {
        assertThat(service.convert(BigDecimal.ZERO,"USD","INR").convertedAmount()).isEqualByComparingTo("0");
        assertThat(service.convert(new BigDecimal("25.5"),"EUR","EUR").convertedAmount()).isEqualByComparingTo("25.50");
    }
    @Test void cachesOneSnapshotAcrossBaseCurrencies() {
        service.rates("EUR");service.rates("USD");service.convert(BigDecimal.TEN,"INR","EUR");
        verify(client,times(1)).fetch();
    }
    @Test void usesExplicitlyMarkedCachedRatesOnTemporaryOutage() {
        service.rates("USD");
        when(clock.instant()).thenReturn(now.plusSeconds(3700));
        when(client.fetch()).thenThrow(new IllegalStateException("offline"));
        assertThat(service.rates("USD").stale()).isTrue();
        service.rates("EUR");
        verify(client,times(2)).fetch();
    }
    @Test void refusesOldCacheAndBacksOffAfterFailure() {
        service.rates("USD");
        when(clock.instant()).thenReturn(now.plus(Duration.ofHours(49)));
        when(client.fetch()).thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(()->service.rates("USD")).isInstanceOf(ExchangeRateService.RatesUnavailableException.class);
        assertThatThrownBy(()->service.rates("EUR")).isInstanceOf(ExchangeRateService.RatesUnavailableException.class);
        verify(client,times(2)).fetch();
    }
    @Test void rejectsInvalidInputsBeforeCallingProvider() {
        assertThatThrownBy(()->service.convert(new BigDecimal("-1"),"USD","EUR")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->service.convert(new BigDecimal("0.0000001"),"USD","EUR")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->service.convert(BigDecimal.ONE,"BAD","EUR")).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(client);
    }
}
