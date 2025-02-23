package org.openweathermap.service;

import static java.lang.String.format;
import static org.openweathermap.CommonConstant.GET_WEATHER_PATH;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openweathermap.entity.SysInfo;
import org.openweathermap.entity.TemperatureInfo;
import org.openweathermap.entity.WeatherData;
import org.openweathermap.entity.WeatherInfo;
import org.openweathermap.entity.WindInfo;
import org.openweathermap.exception.UnexpectedErrorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public class WeatherService {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CompletableFuture<WeatherData> getWeatherData(double lat, double lon, String locale, String apiKey) {
        URI uri = null;
        try {
            uri = new URI(format(GET_WEATHER_PATH, lat, lon, locale, apiKey));
        } catch (URISyntaxException ex) {
            throw new UnexpectedErrorException("Error processing request to OpenWeather: " + ex.getMessage());
        }

        ApiClient client = ApiClient.getInstance();
        CompletableFuture<String> responseFuture = client.getAsync(uri);

        return responseFuture.thenApply(WeatherService::parseResponse);

    }

    private static WeatherData parseResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            WeatherInfo weatherInfo = parseWeatherInfo(rootNode);
            TemperatureInfo temperatureInfo = parseTemperatureInfo(rootNode);
            Long visibility = rootNode.path("visibility").asLong();
            WindInfo windInfo = parseWindInfo(rootNode);
            Long datetime = rootNode.path("dt").asLong();
            SysInfo sysInfo = parseSysInfo(rootNode);
            Integer timezone = rootNode.path("timezone").asInt();
            String name = rootNode.path("name").asText();

            return WeatherData.builder()
                .weather(weatherInfo)
                .temperature(temperatureInfo)
                .visibility(visibility)
                .wind(windInfo)
                .datetime(datetime)
                .sys(sysInfo)
                .timezone(timezone)
                .name(name)
                .build();

        } catch (JsonProcessingException | RuntimeException ex) {
            throw new UnexpectedErrorException("Error while parsing response from OpenWeather service: " + ex.getMessage());
        }
    }

    private static WeatherInfo parseWeatherInfo(JsonNode rootNode) {
        JsonNode weatherNode = rootNode.path("weather").path(0);
        if (weatherNode.isMissingNode()) {
            throw new UnexpectedErrorException("Weather information is missing in the response");
        }
        return WeatherInfo.builder()
            .main(weatherNode.path("main").asText())
            .description(weatherNode.path("description").asText())
            .build();
    }

    private static TemperatureInfo parseTemperatureInfo(JsonNode rootNode) {
        JsonNode mainNode = rootNode.path("main");
        if (mainNode.isMissingNode()) {
            throw new UnexpectedErrorException("Temperature information is missing in the response");
        }
        return TemperatureInfo.builder()
            .temp(mainNode.path("temp").asDouble())
            .feelsLike(mainNode.path("feels_like").asDouble())
            .build();
    }

    private static WindInfo parseWindInfo(JsonNode rootNode) {
        JsonNode windNode = rootNode.path("wind");
        if (windNode.isMissingNode()) {
            throw new UnexpectedErrorException("Wind information is missing in the response");
        }
        return new WindInfo(windNode.path("speed").asDouble());
    }

    private static SysInfo parseSysInfo(JsonNode rootNode) {
        JsonNode sysNode = rootNode.path("sys");
        if (sysNode.isMissingNode()) {
            throw new UnexpectedErrorException("Sys information is missing in the response");
        }
        return SysInfo.builder()
            .sunrise(sysNode.path("sunrise").asLong())
            .sunset(sysNode.path("sunset").asLong())
            .build();
    }
}
