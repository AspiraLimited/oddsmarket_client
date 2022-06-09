package com.aspiralimited.oddsmarket.client.rest;

import com.aspiralimited.oddsmarket.client.rest.dto.BetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.BookmakerDto;
import com.aspiralimited.oddsmarket.client.rest.dto.InternalEventDto;
import com.aspiralimited.oddsmarket.client.rest.dto.LeagueDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketAndBetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketDto;
import com.aspiralimited.oddsmarket.client.rest.dto.PlayerDto;
import com.aspiralimited.oddsmarket.client.rest.dto.ResponseContainer;
import com.aspiralimited.oddsmarket.client.rest.dto.SportDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;

public class OddsmarketRestHttpClient {
    private final String baseUrlMst;
    private final String baseUrlLivePrematch;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);


    public OddsmarketRestHttpClient(String baseUrlMst, String baseUrlLivePrematch, long timeout) {
        this.baseUrlMst = baseUrlMst;
        this.baseUrlLivePrematch = baseUrlLivePrematch;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeout)).build();
    }

    public CompletableFuture<List<BetTypeDto>> getBetTypes() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/bet_types", new TypeReference<ResponseContainer<List<BetTypeDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    public CompletableFuture<List<BookmakerDto>> getBookmakers() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/bookmakers", new TypeReference<ResponseContainer<List<BookmakerDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    public CompletableFuture<List<MarketAndBetTypeDto>> getMarketAndBetTypes() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/market_and_bet_types", new TypeReference<List<MarketAndBetTypeDto>>() {
        });
    }

    public CompletableFuture<List<MarketDto>> getMarkets() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/markets", new TypeReference<ResponseContainer<List<MarketDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    public CompletableFuture<List<SportDto>> getSports() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/sports", new TypeReference<ResponseContainer<List<SportDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    public CompletableFuture<String> getPeriodName(short identifier, short sportId) {
        return getGenericTextEndpoint(baseUrlMst + "/v1/periodName?identifier=" + identifier + "&sportId=" + sportId);
    }

    public CompletableFuture<List<InternalEventDto>> getInternalEvents(Collection<Long> eventIds) {
        return getGenericJsonEndpoint(baseUrlLivePrematch + "/v1/internal_events/" + collectionToCommaSeparatedString(eventIds), new TypeReference<>() {
        });
    }

    public CompletableFuture<List<LeagueDto>> getLeagues(Collection<Long> leagueIds) {
        return getGenericJsonEndpoint(baseUrlLivePrematch + "/v1/leagues/" + collectionToCommaSeparatedString(leagueIds), new TypeReference<>() {
        });
    }

    public CompletableFuture<List<PlayerDto>> getPlayers(Collection<Long> playerIds) {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/players/" + collectionToCommaSeparatedString(playerIds),
                new TypeReference<ResponseContainer<List<PlayerDto>>>() {
                }
        )
                .thenApply(ResponseContainer::getResponse);
    }

    private String collectionToCommaSeparatedString(Collection<Long> longs) {
        return longs.stream().map(x -> Long.toString(x)).collect(Collectors.joining(","));
    }

    private <T> CompletableFuture<T> getGenericJsonEndpoint(String uri, TypeReference<T> typeReference) {
        return get(uri, "application/json")
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        byte[] body = response.body();
                        try {
                            return objectMapper.readValue(body, typeReference);
                        } catch (IOException e) {
                            throw new RuntimeException("Error deserializing response body. Request URI: " + uri + "; response body: " + new String(body), e);
                        }
                    } else {
                        throw new RuntimeException("Invalid HTTP response code: " + response.statusCode()
                                + "; Response body: " + new String(response.body())
                                + "; Request URI: " + response.uri());
                    }
                });
    }

    private CompletableFuture<String> getGenericTextEndpoint(String uri) {
        return get(uri, "text/plain")
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        byte[] body = response.body();
                        return new String(body);
                    } else {
                        throw new RuntimeException("Invalid HTTP response code: " + response.statusCode()
                                + "; Response body: " + new String(response.body())
                                + "; Request URI: " + response.uri());
                    }
                });
    }


    private CompletableFuture<HttpResponse<byte[]>> get(String url, String accept) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("Accept", accept)
                .uri(URI.create(url)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
