package com.aspiralimited.oddsmarket.client;

import com.aspiralimited.oddsmarket.client.rest.OddsmarketRestHttpClient;
import com.aspiralimited.oddsmarket.client.rest.dto.BookmakerDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketAndBetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketDto;
import com.aspiralimited.oddsmarket.client.rest.dto.SportDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class OddsmarketRestHttpClientTest {
    static final int WIREMOCK_PORT = 33456;
    static final String WIREMOCK_BASE_URL = "http://localhost:" + WIREMOCK_PORT;
    private static WireMockServer wireMockServer;

    final OddsmarketRestHttpClient oddsmarketRestHttpClient = new OddsmarketRestHttpClient(WIREMOCK_BASE_URL, WIREMOCK_BASE_URL, WIREMOCK_BASE_URL, 500);

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor(WIREMOCK_PORT);
    }

    @AfterAll
    static void cleanup() {
        wireMockServer.stop();
    }

    @SneakyThrows
    @Test
    void shouldParseBookmakersResponse() {
        makeWiremockStub("/v1/bookmakers", "/rest-response-samples/bookmakers.json");

        List<BookmakerDto> actual = oddsmarketRestHttpClient.getBookmakers().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    @Test
    void shouldParseMarketAndBetTypesResponse() {
        makeWiremockStub("/v1/market_and_bet_types", "/rest-response-samples/market_and_bet_types.json");

        List<MarketAndBetTypeDto> actual = oddsmarketRestHttpClient.getMarketAndBetTypes().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    @Test
    void shouldParseMarketsResponse() {
        makeWiremockStub("/v1/markets", "/rest-response-samples/markets.json");

        List<MarketDto> actual = oddsmarketRestHttpClient.getMarkets().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    @Test
    void shouldParseSportsResponse() {
        makeWiremockStub("/v1/sports", "/rest-response-samples/sports.json");

        List<SportDto> actual = oddsmarketRestHttpClient.getSports().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    private void makeWiremockStub(String url, String filename) {
        byte[] body = getClass().getResourceAsStream(filename).readAllBytes();
        WireMock.stubFor(
                WireMock.get(url)
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(body)
                        )
        );
    }


}