package com.currencyconverter;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProviderOutageTest {
    @Test void returns503WithoutInventingRates() throws Exception {
        RatesClient client=mock(RatesClient.class);when(client.fetch()).thenThrow(new IllegalStateException("offline"));
        var service=new ExchangeRateService(client,Clock.systemUTC());
        var mvc=MockMvcBuilders.standaloneSetup(new CurrencyController(service)).setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/rates/USD")).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.message").value("Exchange rates are temporarily unavailable. Please try again shortly."));
    }
}
