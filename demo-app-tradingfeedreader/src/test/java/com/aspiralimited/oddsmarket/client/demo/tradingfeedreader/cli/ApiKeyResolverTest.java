package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the {@code --apiKey} (literal) and {@code --apiKeyFile} (explicit file) branches of
 * {@link ApiKeyResolver#resolve(String, Path)} plus trim / error-message behaviour.
 *
 * <p>The {@code ODDSMARKET_API_KEY} env-var branch and the {@code ./api-key.txt} default-file
 * branch are intentionally not unit-tested here:
 * <ul>
 *     <li>Setting {@link System#getenv(String)} from a JUnit test requires reflection hacks
 *         (Java 11 has no clean API for it).</li>
 *     <li>The default file is resolved relative to the JVM's CWD, which is fragile to mock
 *         across test environments.</li>
 * </ul>
 * Both paths are exercised by manual / integration runs.
 */
class ApiKeyResolverTest {

    @Test
    void literalApiKey_winsOverExplicitFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("from-file.txt");
        Files.writeString(file, "from-file");

        String result = ApiKeyResolver.resolve("from-literal", file);

        assertEquals("from-literal", result);
    }

    @Test
    void literalApiKey_isTrimmed() {
        assertEquals("abc", ApiKeyResolver.resolve("  abc  ", null));
    }

    @Test
    void literalApiKey_blank_fallsThroughToExplicitFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("k.txt");
        Files.writeString(file, "from-file");

        assertEquals("from-file", ApiKeyResolver.resolve("", file));
        assertEquals("from-file", ApiKeyResolver.resolve("   ", file));
        assertEquals("from-file", ApiKeyResolver.resolve(null, file));
    }

    @Test
    void explicitFile_isRead(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("api.txt");
        Files.writeString(file, "secret-key");

        assertEquals("secret-key", ApiKeyResolver.resolve(null, file));
    }

    @Test
    void explicitFile_trimsTrailingNewline(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("api.txt");
        Files.writeString(file, "secret-key\n");

        assertEquals("secret-key", ApiKeyResolver.resolve(null, file));
    }

    @Test
    void explicitFile_trimsMultipleTrailingNewlines(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("api.txt");
        Files.writeString(file, "secret-key\n\n\n");

        assertEquals("secret-key", ApiKeyResolver.resolve(null, file));
    }

    @Test
    void explicitFile_trimsSurroundingWhitespace(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("api.txt");
        Files.writeString(file, "  secret-key  ");

        assertEquals("secret-key", ApiKeyResolver.resolve(null, file));
    }

    @Test
    void explicitFile_notExists_throwsWithFilePath() {
        Path missing = Paths.get("definitely-does-not-exist-" + System.nanoTime() + ".txt");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ApiKeyResolver.resolve(null, missing));
        assertTrue(ex.getMessage().contains("does not exist"),
                () -> "Message should mention 'does not exist'; got: " + ex.getMessage());
        assertTrue(ex.getMessage().contains(missing.getFileName().toString()),
                () -> "Message should include the file name; got: " + ex.getMessage());
    }

    @Test
    void explicitFile_empty_throws(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ApiKeyResolver.resolve(null, file));
        assertTrue(ex.getMessage().contains("empty"),
                () -> "Message should mention 'empty'; got: " + ex.getMessage());
    }

    @Test
    void explicitFile_onlyWhitespace_throws(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("whitespace.txt");
        Files.writeString(file, "   \n\n  ");

        assertThrows(IllegalArgumentException.class,
                () -> ApiKeyResolver.resolve(null, file));
    }
}
