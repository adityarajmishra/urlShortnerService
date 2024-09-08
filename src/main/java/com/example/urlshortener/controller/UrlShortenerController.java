package com.example.urlshortener.controller;

import com.example.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UrlShortenerController {
    private final UrlShortenerService service;

    public UrlShortenerController(UrlShortenerService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody String originalUrl) {
        try {
            String shortUrl = service.shortenUrl(originalUrl);
            return ResponseEntity.ok(shortUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error shortening URL: " + e.getMessage());
        }
    }

    @GetMapping("/r/{shortUrl}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortUrl) {
        try {
            // Remove URL encoding and any quotation marks
            String decodedShortUrl = URLDecoder.decode(shortUrl, StandardCharsets.UTF_8)
                    .replaceAll("^\"|\"$", "");
            String originalUrl = service.getOriginalUrl(decodedShortUrl);

            if (originalUrl != null && !originalUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", originalUrl)
                        .build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    @GetMapping("/metrics/top-domains")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getTopDomains() {
        return ResponseEntity.ok(service.getTopDomains());
    }
}
