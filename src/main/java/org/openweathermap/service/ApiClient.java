package org.openweathermap.service;

import static org.openweathermap.CommonConstant.MAX_THREADS;

import org.openweathermap.exception.BadRequestException;
import org.openweathermap.exception.OpenWeatherApiException;
import org.openweathermap.exception.TooManyRequestException;
import org.openweathermap.exception.UnauthorizedException;
import org.openweathermap.exception.UnexpectedErrorException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    private static final HttpClient client = HttpClient.newHttpClient();
    private final ExecutorService executorService;

    private ApiClient() {
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<String> getAsync(URI uri) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return handleResponse(response);
            } catch (IOException | InterruptedException ex) {
                throw new UnexpectedErrorException("Error processing request to OpenWeather: " + ex.getMessage());
            }
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    private String handleResponse(HttpResponse<String> response) {
        return switch (response.statusCode()) {
            case 200 -> response.body();
            case 400 -> throw new BadRequestException("Bad request to OpenWeather service");
            case 401 -> throw new UnauthorizedException("Unauthorized request to OpenWeather service");
            case 429 -> throw new TooManyRequestException("Too Many Requests to OpenWeather service");
            default -> throw new OpenWeatherApiException("Error on API OpenWeather service side");
        };
    }

}
