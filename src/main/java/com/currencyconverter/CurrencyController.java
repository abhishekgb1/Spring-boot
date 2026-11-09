package com.currencyconverter;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CurrencyController {
    private final ExchangeRateService service;
    public CurrencyController(ExchangeRateService service) { this.service = service; }
    public record ConversionRequest(
        @NotNull @DecimalMin("0") @DecimalMax("1000000000000") @Digits(integer=13, fraction=6) BigDecimal amount,
        @NotBlank @Size(min=3,max=3) String fromCurrency,
        @NotBlank @Size(min=3,max=3) String toCurrency) {}
    @GetMapping("/currencies") public List<SupportedCurrency> currencies() { return SupportedCurrency.ALL; }
    @GetMapping("/rates/{baseCurrency}") public ExchangeRateService.RatesResult rates(@PathVariable String baseCurrency) { return service.rates(baseCurrency); }
    @PostMapping("/convert") public ExchangeRateService.ConversionResult convert(@Valid @RequestBody ConversionRequest request) {
        return service.convert(request.amount(), request.fromCurrency(), request.toCurrency());
    }
}
