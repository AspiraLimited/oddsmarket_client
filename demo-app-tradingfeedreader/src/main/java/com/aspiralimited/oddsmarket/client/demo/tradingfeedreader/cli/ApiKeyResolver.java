package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.API_KEY_ENV_VAR;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.DEFAULT_API_KEY_FILE;

/**
 * Resolves the API key from one of the supported sources, in priority order:
 * <ol>
 *     <li>Literal value passed via {@code --apiKey} option (or its equivalent in interactive mode).</li>
 *     <li>File path passed via {@code --apiKeyFile} option (or selected interactively).</li>
 *     <li>Environment variable {@value com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants#API_KEY_ENV_VAR}
 *         — suitable for CI/CD and dotenv workflows (see {@code .env.example}).</li>
 *     <li>Default file {@value com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants#DEFAULT_API_KEY_FILE}
 *         in the current working directory — convenient for the portable double-click flow.</li>
 * </ol>
 * The file content is trimmed before use, so trailing newlines are tolerated.
 * <p>
 * Logs which source was used (without printing the key itself) so QA can verify the resolution at startup.
 */
public final class ApiKeyResolver {

    private ApiKeyResolver() {
    }

    public static String resolve(String literalApiKey, Path explicitApiKeyFile) {
        if (literalApiKey != null && !literalApiKey.isBlank()) {
            System.out.println("API key source: --apiKey option");
            return literalApiKey.trim();
        }

        if (explicitApiKeyFile != null) {
            return readFromFile(explicitApiKeyFile, false);
        }

        String envValue = System.getenv(API_KEY_ENV_VAR);
        if (envValue != null && !envValue.isBlank()) {
            System.out.println("API key source: " + API_KEY_ENV_VAR + " environment variable");
            return envValue.trim();
        }

        return readFromFile(Paths.get(DEFAULT_API_KEY_FILE), true);
    }

    private static String readFromFile(Path file, boolean isDefaultFile) {
        if (!Files.exists(file)) {
            if (isDefaultFile) {
                throw new IllegalArgumentException(
                        "No API key provided. Specify --apiKey=<value>, --apiKeyFile=<path>, "
                                + "set the " + API_KEY_ENV_VAR + " environment variable, or create '"
                                + DEFAULT_API_KEY_FILE + "' in the working directory ("
                                + Paths.get("").toAbsolutePath() + ")"
                );
            }
            throw new IllegalArgumentException("API key file does not exist: " + file.toAbsolutePath());
        }

        String content;
        try {
            content = Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read API key from file: " + file.toAbsolutePath(), e);
        }

        if (content.isEmpty()) {
            throw new IllegalArgumentException("API key file is empty: " + file.toAbsolutePath());
        }

        System.out.println("API key source: " + (isDefaultFile ? "default file " : "file ") + file.toAbsolutePath());
        return content;
    }
}
