# Currency Compass

A working Java 17 / Spring Boot currency converter with a responsive frontend, daily exchange rates, precise decimal calculations and automated tests.

This repository originally contained a Maven file and a currency-converter README, but no application source. The missing Java backend and frontend have been implemented. Unused Node, Vite, Drizzle and Replit configurations that referenced missing source folders have been removed. The original files remain in Git history.

## Run locally

Install a JDK 17 or newer and clone this repository. Maven is included through the standard wrapper; the first build needs internet access to download Maven and dependencies.

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```sh
./mvnw spring-boot:run
```

Open **http://localhost:8080**. No API key, database, Node.js or IDE is required.

## What works

- Convert between USD, EUR, GBP, INR, JPY, CAD, AUD, CHF and CNY.
- Swap currencies, view a rates table and see the provider's update timestamp.
- Validate amounts from 0 to 1 trillion with at most six decimal places.
- Calculate with `BigDecimal`, then round to the target currency's minor units (JPY: 0 decimals; the other supported currencies: 2).
- Cache one USD provider snapshot for one hour; derive cross-currency rates from that snapshot.
- On provider failure, use cached data only when the provider timestamp is within 48 hours, mark it as stale, and back off retries for 60 seconds. With no usable snapshot, return HTTP 503 rather than fabricated rates.
- Use mobile layouts, keyboard-accessible forms, loading states, clear errors and a retry control.

## Data source

Uses [ExchangeRate-API's open-access endpoint](https://www.exchangerate-api.com/docs/free): `https://open.er-api.com/v6/latest/USD`.

The provider updates once per day. These are reference estimates, not continuously updated market quotes. Bank and transfer-service rates and fees may differ. Attribution is displayed in the app. A “Check rates” action reads the server cache; it does not bypass the one-hour provider cache.

## Build and tests

```sh
./mvnw -B verify
java -jar target/currency-converter-1.0.0.jar
```

On Windows, use `mvnw.cmd` in place of `./mvnw`. Tests use mock snapshots and a local HTTP stub, so they do not require the external exchange-rate provider. They cover cross rates, target-currency rounding, zero and same-currency conversion, caching, stale-cache fallback, outage handling, request validation, provider-response validation, the frontend resource and health endpoint.

GitHub Actions builds and tests every push to main and every pull request, and uploads the executable JAR as an artifact.

## Docker

```sh
docker compose up --build
```

Or:

```sh
docker build -t currency-compass .
docker run --rm -p 8080:8080 currency-compass
```

The multi-stage image runs the application as a non-root user.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port for local or hosted execution |
| `RATES_PROVIDER_URL` | `https://open.er-api.com/v6/latest/USD` | Provider endpoint; intended for deployment configuration or compatible test services |

For public hosting, deploy the Docker image or executable JAR to a service that runs Java 17. Set the host's health-check path to `/actuator/health`. This is a server application: GitHub Pages cannot execute its Java backend. No public backend deployment is configured by this repository.

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/` | Converter frontend |
| GET | `/api/currencies` | Supported currency codes and names |
| GET | `/api/rates/{baseCurrency}` | Rates against a supported base, `updatedAt` timestamp and `stale` indicator |
| POST | `/api/convert` | Currency conversion with the rate and provider timestamp |
| GET | `/actuator/health` | Application liveness (not provider availability) |

Example request:

```sh
curl -X POST http://localhost:8080/api/convert \
  -H 'Content-Type: application/json' \
  -d '{"amount":100,"fromCurrency":"USD","toCurrency":"INR"}'
```

Success includes `amount`, `fromCurrency`, `toCurrency`, `convertedAmount`, `rate`, `updatedAt` and `stale`. Currency codes are case-insensitive. Invalid inputs return HTTP 400 with a `message`; a provider outage without usable cached data returns HTTP 503.

## Project layout

```text
src/main/java/com/currencyconverter/
  CurrencyConverterApplication.java  Application entry point
  CurrencyController.java            REST endpoints and request validation
  ExchangeRateService.java           Decimal conversion and caching
  HttpRatesClient.java               Provider HTTP client and response validation
  RatesClient.java                   Provider interface
  RateSnapshot.java                  Immutable rate snapshot
  SupportedCurrency.java             Supported codes and names
  ApiExceptionHandler.java           Consistent API errors
src/main/resources/
  application.properties             Runtime configuration
  static/                            HTML, CSS and JavaScript frontend
src/test/java/com/currencyconverter/  Service, API, provider and outage tests
.github/workflows/ci.yml              Automated Maven verification
Dockerfile                           Container build
```

## Troubleshooting

- **Java not found:** install JDK 17+ and verify `java -version`.
- **First build cannot download files:** check access to Maven Central. Dependencies are cached after download.
- **Port occupied:** set `PORT` to another value before starting the app.
- **Rates unavailable:** check outbound HTTPS access to the provider. The app reports outages and allows retry; it does not use invented fallback values.
