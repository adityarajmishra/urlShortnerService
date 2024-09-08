package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
public class UrlShortenerService {
    private final UrlMappingRepository repository;
    private final Map<String, String> urlCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> domainCount = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerService.class);

    public UrlShortenerService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    public String shortenUrl(String originalUrl) {
        if (urlCache.containsKey(originalUrl)) {
            return urlCache.get(originalUrl);
        }

        String encodedUrl = Base64.getUrlEncoder().encodeToString(originalUrl.getBytes(StandardCharsets.UTF_8));
        String shortUrl = encodedUrl.substring(0, 8); // Use first 8 characters for shorter URL

        UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);
        repository.save(mapping);
        urlCache.put(originalUrl, shortUrl);

        String domain = extractDomain(originalUrl);
        domainCount.merge(domain, 1, Integer::sum);

        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        logger.info("Attempting to retrieve original URL for short URL: {}", shortUrl);
        String originalUrl = urlCache.entrySet().stream()
                .filter(entry -> entry.getValue().equals(shortUrl))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseGet(() -> {
                    logger.info("Short URL not found in cache, checking database");
                    return repository.findByShortUrl(shortUrl)
                            .map(UrlMapping::getOriginalUrl)
                            .orElseThrow(() -> {
                                logger.error("URL not found for short URL: {}", shortUrl);
                                return new RuntimeException("URL not found");
                            });
                });
        logger.info("Retrieved original URL: {}", originalUrl);
        return originalUrl;
    }

    public List<Map.Entry<String, Integer>> getTopDomains() {
        return domainCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    private String extractDomain(String url) {
        String domain = url.replaceFirst("^(https?://)?www\\.", "")
                .split("/")[0];
        return domain.substring(domain.lastIndexOf('.', domain.lastIndexOf('.') - 1) + 1);
    }
}