package com.example.urlshortener.service;

import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    // Method to shorten a URL
    public String shortenUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            logger.error("Empty or null URL provided.");
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        String domain = extractDomain(originalUrl);

        // Increment domain count in a thread-safe manner using ConcurrentHashMap.merge
        domainCount.merge(domain, 1, Integer::sum);

        // Check if URL is already shortened and cached
        if (urlCache.containsKey(originalUrl)) {
            return urlCache.get(originalUrl);  // Return cached short URL
        }

        try {
            // Generate a unique short URL if not cached
            String shortUrl = generateShortUrl(originalUrl);

            // Persist the mapping to the database
            UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);
            repository.save(mapping);

            // Cache the result for future requests
            urlCache.put(originalUrl, shortUrl);

            return shortUrl;
        } catch (Exception e) {
            logger.error("Error shortening URL for {}: {}", originalUrl, e.getMessage());
            throw new RuntimeException("Error shortening URL", e);
        }
    }

    // Retrieve the original URL from the short URL
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

    // Return the top 3 domains from shortened URLs
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

    // Generate a unique short URL
    private String generateShortUrl(String originalUrl) throws Exception {
        // Use a hash (e.g., MD5) to create a unique ID for the short URL
        byte[] hash = MessageDigest.getInstance("MD5").digest(originalUrl.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().encodeToString(Arrays.copyOf(hash, 6));
    }

    // Extract domain from the given URL
    public String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            logger.warn("Empty or null URL passed. Returning default domain.");
            return "invalid-domain.com";  // Fallback for empty or null URLs
        }

        try {
            // Remove any surrounding quotation marks
            url = url.replaceAll("^\"|\"$", "");

            // Improved regex to match domain in URL, supporting both 'http' and 'https', with or without 'www'
            Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?([^:/\\n]+)");
            Matcher matcher = pattern.matcher(url);

            if (matcher.find()) {
                String domain = matcher.group(1);  // Extract the domain part

                // Remove any trailing quotation marks or backslashes
                domain = domain.replaceAll("[\"\\\\]+$", "");

                // Handle any potential subdomains or return as-is
                String[] domainParts = domain.split("\\.");
                int length = domainParts.length;

                if (length > 2) {
                    // Example: For 'www.youtube.co.uk', this will return 'youtube.co.uk'
                    return domainParts[length - 2] + "." + domainParts[length - 1];
                } else {
                    return domain;  // Example: For 'youtube.com', return 'youtube.com'
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting domain from URL '{}': {}", url, e.getMessage());
        }

        return "invalid-domain.com";  // Return fallback if extraction fails
    }

}
