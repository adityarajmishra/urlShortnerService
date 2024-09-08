package com.example.urlshortener;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlShortenerServiceTests {

	@Mock
	private UrlMappingRepository repository;

	@InjectMocks
	private UrlShortenerService service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void shortenUrl_NewUrl_Success() {
		String originalUrl = "https://www.example.com";
		String shortUrl = "abcdefgh";
		UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);

		when(repository.save(any(UrlMapping.class))).thenReturn(mapping);

		String result = service.shortenUrl(originalUrl);

		assertNotNull(result);
		assertEquals(8, result.length());
		verify(repository, times(1)).save(any(UrlMapping.class));
	}

	@Test
	void shortenUrl_ExistingUrl_ReturnsCachedShortUrl() {
		String originalUrl = "https://www.example.com";
		String shortUrl = "abcdefgh";

		// First call to shortenUrl
		when(repository.save(any(UrlMapping.class))).thenReturn(new UrlMapping(originalUrl, shortUrl));
		String firstResult = service.shortenUrl(originalUrl);

		// Second call to shortenUrl with the same URL
		String secondResult = service.shortenUrl(originalUrl);

		assertEquals(firstResult, secondResult);
		verify(repository, times(1)).save(any(UrlMapping.class)); // Repository should be called only once
	}

	@Test
	void getOriginalUrl_ExistingShortUrl_ReturnsOriginalUrl() {
		String originalUrl = "https://www.example.com";
		String shortUrl = "abcdefgh";
		UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);

		when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(mapping));

		String result = service.getOriginalUrl(shortUrl);

		assertEquals(originalUrl, result);
		verify(repository, times(1)).findByShortUrl(shortUrl);
	}

	@Test
	void getOriginalUrl_NonExistentShortUrl_ThrowsException() {
		String shortUrl = "nonexistent";

		when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> service.getOriginalUrl(shortUrl));
		verify(repository, times(1)).findByShortUrl(shortUrl);
	}

	@Test
	void getTopDomains_ReturnsCorrectOrder() {
		// Simulate multiple URL shortenings
		service.shortenUrl("https://www.example1.com");
		service.shortenUrl("https://www.example2.com");
		service.shortenUrl("https://www.example2.com");
		service.shortenUrl("https://www.example3.com");
		service.shortenUrl("https://www.example3.com");
		service.shortenUrl("https://www.example3.com");

		List<Map.Entry<String, Integer>> topDomains = service.getTopDomains();

		assertEquals(3, topDomains.size());
		assertEquals("example3.com", topDomains.get(0).getKey());
		assertEquals(3, topDomains.get(0).getValue());
		assertEquals("example2.com", topDomains.get(1).getKey());
		assertEquals(2, topDomains.get(1).getValue());
		assertEquals("example1.com", topDomains.get(2).getKey());
		assertEquals(1, topDomains.get(2).getValue());
	}

	@Test
	void shortenUrl_MultipleCalls_IncrementsDomainCount() {
		service.shortenUrl("https://www.example.com/page1");
		service.shortenUrl("https://www.example.com/page2");
		service.shortenUrl("https://www.otherdomain.com");

		List<Map.Entry<String, Integer>> topDomains = service.getTopDomains();

		assertEquals(2, topDomains.size());
		assertEquals("example.com", topDomains.get(0).getKey());
		assertEquals(2, topDomains.get(0).getValue());
		assertEquals("otherdomain.com", topDomains.get(1).getKey());
		assertEquals(1, topDomains.get(1).getValue());
	}
}