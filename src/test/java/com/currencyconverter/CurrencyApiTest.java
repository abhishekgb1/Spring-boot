package com.currencyconverter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean RatesClient client;
    @Autowired ExchangeRateService service;
    @BeforeEach void setup() {
        Instant now=Instant.now();
        when(client.fetch()).thenReturn(new RateSnapshot(Map.of("USD",BigDecimal.ONE,"EUR",new BigDecimal("0.8"),"INR",new BigDecimal("80"),"JPY",new BigDecimal("150"),"GBP",new BigDecimal("0.7"),"CAD",new BigDecimal("1.4"),"AUD",new BigDecimal("1.5"),"CHF",new BigDecimal("0.9"),"CNY",new BigDecimal("7")),now,now));
    }
    @Test void servesApplicationAndHealth() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Currency Compass")));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
    @Test void listsAllNineCurrencies() throws Exception { mvc.perform(get("/api/currencies")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(9)); }
    @Test void convertsAndExposesRates() throws Exception {
        mvc.perform(post("/api/convert").contentType(MediaType.APPLICATION_JSON).content("{\"amount\":10,\"fromCurrency\":\"EUR\",\"toCurrency\":\"INR\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.convertedAmount").value(1000)).andExpect(jsonPath("$.stale").value(false));
        mvc.perform(get("/api/rates/EUR")).andExpect(status().isOk()).andExpect(jsonPath("$.rates.USD").value(1.25));
    }
    @Test void rejectsMissingNegativeOversizedAndUnsupportedInputs() throws Exception {
        for(String json:new String[]{"{}","{\"amount\":-1,\"fromCurrency\":\"USD\",\"toCurrency\":\"EUR\"}","{\"amount\":1000000000001,\"fromCurrency\":\"USD\",\"toCurrency\":\"EUR\"}","{\"amount\":1,\"fromCurrency\":\"BAD\",\"toCurrency\":\"EUR\"}","not-json"}) {
            mvc.perform(post("/api/convert").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        }
        mvc.perform(get("/api/rates/BAD")).andExpect(status().isBadRequest());
    }
}
