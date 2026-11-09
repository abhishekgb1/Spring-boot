package com.currencyconverter;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String,String>> invalid(Exception error) {
        String message = error instanceof IllegalArgumentException ? error.getMessage() : "Provide a valid amount (0–1 trillion, up to 6 decimals) and two supported currency codes.";
        return ResponseEntity.badRequest().body(Map.of("message",message));
    }
    @ExceptionHandler(ExchangeRateService.RatesUnavailableException.class)
    public ResponseEntity<Map<String,String>> unavailable(RuntimeException error) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message",error.getMessage()));
    }
}
