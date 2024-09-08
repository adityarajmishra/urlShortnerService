package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Base64;

@Service
public class UrlShortenerService {
    private final UrlMappingRepository repository;

    public UrlShortenerService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    public String shortenUrl(String originalUrl) {
        Optional<UrlMapping> existingMapping = repository.findByOriginalUrl(originalUrl);
        if (existingMapping.isPresent()) {
            return existingMapping.get().getShortUrl();
        }

        // Fix: Convert hashCode to String before encoding
        String hashString = Integer.toString(originalUrl.hashCode());
        String shortUrl = Base64.getUrlEncoder().encodeToString(hashString.getBytes(StandardCharsets.UTF_8));

        UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);
        repository.save(mapping);
        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        return repository.findByShortUrl(shortUrl)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }
}
