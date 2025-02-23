package org.openweathermap.service;

import static java.lang.String.format;
import static org.openweathermap.CommonConstant.DEFAULT_LOCALE;
import static org.openweathermap.CommonConstant.GET_COORDINATES_BY_LOCATION_PATH;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openweathermap.entity.City;
import org.openweathermap.exception.NotFoundException;
import org.openweathermap.exception.UnexpectedErrorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public class GeoCodingService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CompletableFuture<City> findCity(String questCity, String locale, String apiKey) {
        URI uri = null;
        try {
            uri = new URI(format(GET_COORDINATES_BY_LOCATION_PATH, questCity, apiKey));
        } catch (URISyntaxException ex) {
            throw new UnexpectedErrorException("Error processing request to GeoCoding service: " + ex.getMessage());
        }

        ApiClient client = ApiClient.getInstance();
        CompletableFuture<String> responseFuture = client.getAsync(uri);

        return responseFuture.thenApply(
            response -> parseResponse(response, locale)
        );
    }

    private static City parseResponse(String jsonResponse, String locale) {
        try {
            JsonNode array = objectMapper.readTree(jsonResponse);
            if (!array.isArray() | array.isEmpty()) {
                throw new NotFoundException("City not found at GeoCoding service");
            }

            JsonNode cityData = array.get(0);
            if (cityData == null) {
                throw new UnexpectedErrorException("Invalid response format from GeoCoding service: Missing city data");
            }

            String name = cityData.has("name") ? cityData.get("name").asText() : null;
            String country = cityData.has("country") ? cityData.get("country").asText() : null;
            Double lat = cityData.has("lat") ? cityData.get("lat").asDouble() : null;
            Double lon = cityData.has("lon") ? cityData.get("lon").asDouble() : null;
            String state = cityData.has("state") ? cityData.get("state").asText() : null;

            if (name == null || country == null || lat == null || lon == null) {
                throw new UnexpectedErrorException("Incomplete city data received from GeoCoding service");
            }

            return City.builder()
                .name(name)
                .localeName(getLocalCityName(cityData, locale))
                .country(country)
                .state(state)
                .lat(lat)
                .lon(lon)
                .build();

        } catch (JsonProcessingException ex) {
            throw new UnexpectedErrorException("Error while parsing response from GeoCoding service: " + ex.getMessage());
        }
    }

    private static String getLocalCityName(JsonNode cityData, String locale) {
        if (cityData.has("local_names")) {
            JsonNode localNames = cityData.get("local_names");
            String lowerLocale = locale.toLowerCase();
            if (localNames != null) {
                if (localNames.has(lowerLocale)) {
                    return localNames.get(lowerLocale).asText();
                } else if (localNames.has(DEFAULT_LOCALE)) {
                    return localNames.get(DEFAULT_LOCALE).asText();
                }
            }
        }
        return cityData.get("name").asText();
    }

}
