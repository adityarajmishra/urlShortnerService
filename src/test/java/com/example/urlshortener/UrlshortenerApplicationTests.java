package com.example.urlshortener;

import com.example.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UrlShortenerApplicationTests {

	@Autowired
	private UrlShortenerService service;

	@Test
	void shortenUrlTest() {
		String originalUrl = "https://example.com";
		String shortUrl = service.shortenUrl(originalUrl);
		assertEquals(originalUrl, service.getOriginalUrl(shortUrl));
	}
}
