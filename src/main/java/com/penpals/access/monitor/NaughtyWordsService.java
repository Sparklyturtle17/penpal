package com.penpals.access.monitor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads the moderation word list from the classpath once at startup. Kept server-side
 * so the list is only ever exposed through the monitor/admin-gated endpoint, never
 * bundled into a penpal's or guardian's browser.
 */
@Service
public class NaughtyWordsService {

	private final List<String> words;

	public NaughtyWordsService() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new ClassPathResource("naughtyWords.txt").getInputStream(), StandardCharsets.UTF_8))) {
			this.words = reader.lines()
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.map(w -> w.toLowerCase())
				.distinct()
				.toList();
		} catch (IOException e) {
			throw new UncheckedIOException("Could not load naughtyWords.txt", e);
		}
	}

	public List<String> words() {
		return words;
	}
}
