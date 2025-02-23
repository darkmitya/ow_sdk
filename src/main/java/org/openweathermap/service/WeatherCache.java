package org.openweathermap.service;

import static org.openweathermap.CommonConstant.CACHE_TIME_TO_LIVE_MSEC;
import static org.openweathermap.CommonConstant.MAX_CACHE_SIZE;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.openweathermap.entity.WeatherData;

import java.util.concurrent.TimeUnit;

public class WeatherCache {

    private static WeatherCache sharedInstance;
    private final Cache<String, WeatherData> weatherCache;

    private WeatherCache() {
        this.weatherCache = CacheBuilder.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterWrite(CACHE_TIME_TO_LIVE_MSEC, TimeUnit.MILLISECONDS)
            .build();
    }

    public static synchronized WeatherCache getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new WeatherCache();
        }
        return sharedInstance;
    }

    public static WeatherCache getInstance() {
        return new WeatherCache();
    }

    public WeatherData getValue(String key) {
        return weatherCache.getIfPresent(key);
    }

    public void push(String key, WeatherData value) {
        weatherCache.put(key, value);
    }

    public void remove(String key) {
        weatherCache.invalidate(key);
    }
}
