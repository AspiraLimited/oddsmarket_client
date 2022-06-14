package com.aspiralimited.oddsmarket.client;

import com.aspiralimited.oddsmarket.client.rest.OddsmarketRestHttpClient;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.BookmakerDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.InternalEventDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.LeagueDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.MarketAndBetTypeDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.MarketDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.PlayerDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.SportDto;
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

    final OddsmarketRestHttpClient oddsmarketRestHttpClient = new OddsmarketRestHttpClient(WIREMOCK_BASE_URL, WIREMOCK_BASE_URL, 500);

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
    @Test
    void shouldParsePeriodNameResponse() {
        makeWiremockStub("/v1/periodName?identifier=0&sportId=6", "/rest-response-samples/periodName.txt");

        String actual = oddsmarketRestHttpClient.getPeriodName((short) 0, (short) 6).get();
        Assertions.assertEquals("regular time", actual);
    }

    @SneakyThrows
    @Test
    void shouldParseInternalEventsResponse() {
        makeWiremockStub("/v1/internal_events/374092363,374518899", "/rest-response-samples/internal_events.json");

        List<InternalEventDto> actual = oddsmarketRestHttpClient.getInternalEvents(List.of(374092363L, 374518899L)).get();
        Assertions.assertEquals(2, actual.size());
    }

    @SneakyThrows
    @Test
    void shouldParseLeaguesResponse() {
        makeWiremockStub("/v1/leagues/5944,8719", "/rest-response-samples/leagues.json");

        List<LeagueDto> actual = oddsmarketRestHttpClient.getLeagues(List.of(5944L, 8719L)).get();
        Assertions.assertEquals(2, actual.size());
    }

    @SneakyThrows
    @Test
    void shouldParsePlayersResponse() {
        makeWiremockStub("/v1/players/3654837", "/rest-response-samples/players.json");

        List<PlayerDto> actual = oddsmarketRestHttpClient.getPlayers(List.of(3654837L)).get();
        Assertions.assertEquals(1, actual.size());
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
