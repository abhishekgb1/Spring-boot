package com.currencyconverter;

import java.util.List;
import java.util.Locale;

public record SupportedCurrency(String code, String name) {
    public static final List<SupportedCurrency> ALL = List.of(
        new SupportedCurrency("USD", "US Dollar"), new SupportedCurrency("EUR", "Euro"),
        new SupportedCurrency("GBP", "British Pound"), new SupportedCurrency("INR", "Indian Rupee"),
        new SupportedCurrency("JPY", "Japanese Yen"), new SupportedCurrency("CAD", "Canadian Dollar"),
        new SupportedCurrency("AUD", "Australian Dollar"), new SupportedCurrency("CHF", "Swiss Franc"),
        new SupportedCurrency("CNY", "Chinese Yuan"));
    public static String require(String value) {
        String code = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (ALL.stream().noneMatch(currency -> currency.code.equals(code))) {
            throw new IllegalArgumentException("Choose a supported currency: USD, EUR, GBP, INR, JPY, CAD, AUD, CHF or CNY.");
        }
        return code;
    }
}
