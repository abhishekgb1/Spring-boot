package com.currencyconverter;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpRatesClient implements RatesClient {
    private final RestClient client;
    private final Clock clock;
    public HttpRatesClient(@Value("${rates.provider-url}") String url, Clock clock) {
        this.clock = clock;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    @Override public RateSnapshot fetch() {
        JsonNode body = client.get().retrieve().body(JsonNode.class);
        if (body == null || !"success".equals(body.path("result").asText()) || !"USD".equals(body.path("base_code").asText())) {
            throw new IllegalStateException("Invalid exchange-rate provider response");
        }
        var rates = new LinkedHashMap<String, BigDecimal>();
        for (var currency : SupportedCurrency.ALL) {
            JsonNode value = body.path("rates").path(currency.code());
            if (!value.isNumber() || value.decimalValue().signum() <= 0) throw new IllegalStateException("Missing or invalid exchange rate");
            rates.put(currency.code(), value.decimalValue());
        }
        if (rates.get("USD").compareTo(BigDecimal.ONE) != 0) throw new IllegalStateException("Invalid base exchange rate");
        long timestamp = body.path("time_last_update_unix").asLong(0);
        Instant updated = Instant.ofEpochSecond(timestamp);
        Instant now = clock.instant();
        if (timestamp <= 0 || updated.isAfter(now.plusSeconds(300))) throw new IllegalStateException("Invalid rate timestamp");
        return new RateSnapshot(rates, updated, now);
    }
}
