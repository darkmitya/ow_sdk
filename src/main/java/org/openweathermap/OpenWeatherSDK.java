package org.openweathermap;

import static org.openweathermap.CommonConstant.CACHE_TIME_TO_LIVE_MSEC;
import static org.openweathermap.CommonConstant.DEFAULT_LOCALE;

import org.openweathermap.entity.City;
import org.openweathermap.entity.UpdateMode;
import org.openweathermap.entity.WeatherData;
import org.openweathermap.exception.UnexpectedErrorException;
import org.openweathermap.service.GeoCodingService;
import org.openweathermap.service.WeatherCache;
import org.openweathermap.service.WeatherService;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;


/**
 * OpenWeatherSDK - A Java SDK for interacting with OpenWeather API.
 * This class provides methods to fetch weather data for a given city.
 */
public class OpenWeatherSDK {

    private final WeatherCache cache;
    private final String apiKey;
    private final String locale;

    private final UpdateMode mode;
    private final ConcurrentSkipListMap<Instant, City> cityList = new ConcurrentSkipListMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructs a new OpenWeatherSDK instance with the specified API key, update mode, and locale.
     *
     * @param apiKey The API key to use for accessing the OpenWeather API. Cannot be empty or null.
     * @param mode The update mode to use, either ON_DEMAND or POLLING.
     * @param locale The locale to use for localized responses. If null, a default locale is used 'en'.
     * @throws IllegalArgumentException if the API key is empty or null.
     */
    public OpenWeatherSDK(String apiKey, UpdateMode mode, @Nullable String locale) {
        if (apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be empty");
        }

        this.locale = Optional.ofNullable(locale).orElse(DEFAULT_LOCALE);

        if (UpdateMode.ON_DEMAND.equals(mode)) {
            cache = WeatherCache.getInstance();
        } else {
            cache = WeatherCache.getSharedInstance();
        }
        this.apiKey = apiKey;
        this.mode = mode;

        scheduler.schedule(this::updateWeather, CACHE_TIME_TO_LIVE_MSEC, TimeUnit.MILLISECONDS);
    }

    /**
     * Retrieves the weather data for the specified city. If the data is available in the cache,
     * it is returned immediately. Otherwise, the method fetches the data from the OpenWeather API.
     *
     * @param cityName The name of the city for which to retrieve weather data.
     * @return The weather data for the specified city.
     * @throws UnexpectedErrorException if there is an error while fetching the weather data.
     */
    public WeatherData getWeather(String cityName) {
        WeatherData data = cache.getValue(cityName);
        if (data != null) {
            return data;
        }

        try {
            return GeoCodingService.findCity(cityName, locale, apiKey)
                .thenCompose(this::getWeatherByCity)
                .join();
        } catch (CompletionException e) {
            throw new UnexpectedErrorException("Failed to get weather data:" + e.getCause());
        }
    }

    /**
     * Clears the list of requested cached cities.
     */
    public void cleanCashedCity() {
        cityList.clear();
    }

    private CompletableFuture<WeatherData> getWeatherByCity(City city) {
        if (mode.equals(UpdateMode.ON_DEMAND)) {
            cityList.put(Instant.now().plusMillis(CACHE_TIME_TO_LIVE_MSEC), city);
        }

        WeatherData weatherDataFromCache = cache.getValue(city.getName());
        if (weatherDataFromCache != null) {
            return CompletableFuture.completedFuture(weatherDataFromCache);
        }

        return WeatherService.getWeatherData(city.getLat(), city.getLon(), locale, apiKey)
            .thenApply(weatherData -> {
                cache.push(city.getName(), weatherData);
                return weatherData;
            });
    }

    private void updateWeather() {
        Instant nextUpdate = Instant.now().plusMillis(CACHE_TIME_TO_LIVE_MSEC);
        if (!cityList.isEmpty()) {
            cityList.headMap(Instant.now().plusMillis(100L))
                .forEach((k, v) -> {
                    cache.remove(v.getName());
                    getWeatherByCity(v);
                    cityList.remove(k);
                });
            nextUpdate = cityList.firstKey();
        }

        long delayMlS = Duration.between(nextUpdate, Instant.now()).toMillis();
        if (delayMlS < 0) {
            delayMlS = 0;
        }
        scheduler.schedule(this::updateWeather, delayMlS, TimeUnit.MILLISECONDS);
    }

}