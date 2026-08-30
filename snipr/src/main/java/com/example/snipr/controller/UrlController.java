package com.example.snipr.controller;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.snipr.model.UrlMapping;
import com.example.snipr.service.UrlShortenerService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UrlController {

    private final UrlShortenerService service;

    public UrlController(UrlShortenerService service) {
        this.service = service;
    }

    public record ShortenRequest(String longUrl) {}
    public record ShortenResponse(String shortUrl, String longUrl) {}

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shorten(@RequestBody ShortenRequest request) {
        if (request.longUrl() == null || request.longUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "longUrl is required"));
        }

        UrlMapping mapping = service.createShortUrl(request.longUrl());
        ShortenResponse response = new ShortenResponse(
                service.getFullShortUrl(mapping),
                mapping.getLongUrl()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * NEW: the actual redirect. GET /{code}, e.g. GET /1
     *
     * HttpServletRequest gives us access to the raw incoming request --
     * headers, IP, etc. Spring injects it automatically as a method
     * parameter, same dependency-injection idea as everywhere else.
     *
     * HttpStatus.FOUND = HTTP 302 ("temporary redirect"). We use 302, not
     * 301 ("permanent redirect"), on purpose: a 301 tells browsers "cache
     * this forever, never ask my server again" -- which would mean the
     * browser stops hitting OUR server at all after the first click, and
     * we'd lose the ability to log clicks or track analytics. 302 keeps
     * every single click flowing through our server.
     */
    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code, HttpServletRequest request) {
        Optional<String> longUrl = service.resolveLongUrl(code);

        if (longUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.recordClick(
                code,
                request.getHeader("Referer"), // yes, "Referer" -- a 30-year-old HTTP spec typo we're stuck with
                request.getHeader("User-Agent"),
                request.getRemoteAddr()
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl.get()))
                .build();
    }
}