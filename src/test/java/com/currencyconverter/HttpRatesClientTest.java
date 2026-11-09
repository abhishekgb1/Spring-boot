package com.currencyconverter;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.*;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

class HttpRatesClientTest {
    private HttpServer server;
    private String body;
    private final Instant now=Instant.parse("2026-11-09T00:00:00Z");
    @BeforeEach void setup() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{byte[] bytes=body.getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().add("Content-Type","application/json");exchange.sendResponseHeaders(200,bytes.length);try(var output=exchange.getResponseBody()){output.write(bytes);}});
        server.start();
    }
    @AfterEach void cleanup(){server.stop(0);}
    private HttpRatesClient client(){return new HttpRatesClient("http://127.0.0.1:"+server.getAddress().getPort(),Clock.fixed(now,ZoneOffset.UTC));}
    private String valid(){return "{\"result\":\"success\",\"base_code\":\"USD\",\"time_last_update_unix\":"+now.getEpochSecond()+",\"rates\":{\"USD\":1,\"EUR\":0.8,\"INR\":80,\"JPY\":150,\"GBP\":0.7,\"CAD\":1.4,\"AUD\":1.5,\"CHF\":0.9,\"CNY\":7}}";}
    @Test void parsesProviderSnapshot(){body=valid();var snapshot=client().fetch();assertThat(snapshot.rates()).hasSize(9);assertThat(snapshot.updatedAt()).isEqualTo(now);}
    @Test void rejectsProviderError(){body="{\"result\":\"error\"}";assertThatThrownBy(()->client().fetch()).isInstanceOf(IllegalStateException.class);}
    @Test void rejectsNegativeRates(){body=valid().replace("\"EUR\":0.8","\"EUR\":-1");assertThatThrownBy(()->client().fetch()).isInstanceOf(IllegalStateException.class);}
}
