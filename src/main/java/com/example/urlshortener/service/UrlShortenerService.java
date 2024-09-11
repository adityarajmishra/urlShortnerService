package com.example.urlshortener.service;

import com.example.urlshortener.exception.UrlNotFoundException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String domain = extractDomain(originalUrl);
        domainCount.merge(domain, 1, Integer::sum);
        
        if (urlCache.containsKey(originalUrl)) {
            return urlCache.get(originalUrl);
        }

        try {
            String encodedUrl = Base64.getUrlEncoder().encodeToString(originalUrl.getBytes(StandardCharsets.UTF_8));
            String shortUrl = encodedUrl.substring(0, 8); // Use first 8 characters for shorter URL

            UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);
            repository.save(mapping);
            urlCache.put(originalUrl, shortUrl);

            return shortUrl;
        } catch (Exception e) {
            logger.error("Error shortening URL: {}", e.getMessage());
            throw new RuntimeException("Error shortening URL", e);
        }
    }

    public String getOriginalUrl(String shortUrl) {
        logger.info("Attempting to retrieve original URL for short URL: {}", shortUrl);
        return urlCache.entrySet().stream()
                .filter(entry -> entry.getValue().equals(shortUrl))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseGet(() -> {
                    logger.info("Short URL not found in cache, checking database");
                    return repository.findByShortUrl(shortUrl)
                            .map(UrlMapping::getOriginalUrl)
                            .orElseThrow(() -> {
                                logger.error("URL not found for short URL: {}", shortUrl);
                                return new UrlNotFoundException("URL not found for short URL: " + shortUrl);
                            });
                });
    }

    public List<Map.Entry<String, Integer>> getTopDomains() {
        return domainCount.entrySet().stream()
                .sorted((e1, e2) -> {
                    int valueComparison = e2.getValue().compareTo(e1.getValue());
                    if (valueComparison != 0) {
                        return valueComparison;
                    }
                    return e1.getKey().compareTo(e2.getKey());
                })
                .limit(3)
                .collect(Collectors.toList());
    }

    private String extractDomain(String url) {
        try {
            // Use regex to extract domain more reliably
            Pattern pattern = Pattern.compile("^(https?://)?(www\\.)?([^/]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String domain = matcher.group(3);
                int lastDotIndex = domain.lastIndexOf('.');
                return domain.substring(domain.lastIndexOf('.', lastDotIndex - 1) + 1);
            }
        } catch (Exception e) {
            logger.error("Error extracting domain: {}", e.getMessage());
        }
        return "";
    }
}
