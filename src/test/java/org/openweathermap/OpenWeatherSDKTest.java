package org.openweathermap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openweathermap.TestObjectFactory.API_KEY;
import static org.openweathermap.TestObjectFactory.CITY_API_RESPONSE_MOSCOW;
import static org.openweathermap.TestObjectFactory.LOCALE_EN;
import static org.openweathermap.TestObjectFactory.LOCALE_RU;
import static org.openweathermap.TestObjectFactory.NAME_CITY_MOSCOW_EN;
import static org.openweathermap.TestObjectFactory.NAME_CITY_MOSCOW_RU;
import static org.openweathermap.TestObjectFactory.NAME_INCORRECT_CITY_2;
import static org.openweathermap.TestObjectFactory.REQUEST_URI_CITY_MOSCOW_EN;
import static org.openweathermap.TestObjectFactory.REQUEST_URI_CITY_MOSCOW_RU;
import static org.openweathermap.TestObjectFactory.REQUEST_URI_WEATHER_MOSCOW_EN;
import static org.openweathermap.TestObjectFactory.REQUEST_URI_WEATHER_MOSCOW_RU;
import static org.openweathermap.TestObjectFactory.WEATHER_API_RESPONSE_MOSCOW;
import static org.openweathermap.TestObjectFactory.WEATHER_RESPONSE_MOSCOW;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openweathermap.entity.UpdateMode;
import org.openweathermap.entity.WeatherData;
import org.openweathermap.exception.UnexpectedErrorException;
import org.openweathermap.service.ApiClient;
import org.openweathermap.service.GeoCodingService;
import org.openweathermap.service.WeatherCache;

import java.util.concurrent.CompletableFuture;


class OpenWeatherSDKTest {


    @Test
    void test_getWeather_ok() {

        try (MockedStatic<ApiClient> apiClientMock = mockStatic(ApiClient.class)) {
            ApiClient mockApiClient = mock(ApiClient.class);
            apiClientMock.when(ApiClient::getInstance).thenReturn(mockApiClient);

            when(mockApiClient.getAsync(REQUEST_URI_CITY_MOSCOW_RU))
                .thenReturn(CompletableFuture.completedFuture(CITY_API_RESPONSE_MOSCOW));
            when(mockApiClient.getAsync(REQUEST_URI_CITY_MOSCOW_EN))
                .thenReturn(CompletableFuture.completedFuture(CITY_API_RESPONSE_MOSCOW));
            when(mockApiClient.getAsync(REQUEST_URI_WEATHER_MOSCOW_RU))
                .thenReturn(CompletableFuture.completedFuture(WEATHER_API_RESPONSE_MOSCOW));

            OpenWeatherSDK sdk = new OpenWeatherSDK(API_KEY, UpdateMode.ON_DEMAND, LOCALE_RU);

            WeatherData data = sdk.getWeather(NAME_CITY_MOSCOW_EN);

            System.out.println("Weather in " + data.getName() + ": "+ data.getWeather().getDescription() + " temp:" + data.getTemperature().getTemp() + "C, ");

            assertEquals(WEATHER_RESPONSE_MOSCOW, data);
        }
    }

    @Test
    void getWeather_ShouldReturnFromCache_WhenDataIsCached() {

        try (MockedStatic<ApiClient> apiClientMock = mockStatic(ApiClient.class);
             MockedStatic<WeatherCache> weatherCacheMock = mockStatic(WeatherCache.class)) {
            ApiClient mockApiClient = mock(ApiClient.class);
            WeatherCache weatherCache = spy(WeatherCache.class);

            apiClientMock.when(ApiClient::getInstance).thenReturn(mockApiClient);
            weatherCacheMock.when(WeatherCache::getSharedInstance).thenReturn(weatherCache);

            when(mockApiClient.getAsync(REQUEST_URI_CITY_MOSCOW_RU))
                .thenReturn(CompletableFuture.completedFuture(CITY_API_RESPONSE_MOSCOW));
            when(mockApiClient.getAsync(REQUEST_URI_CITY_MOSCOW_EN))
                .thenReturn(CompletableFuture.completedFuture(CITY_API_RESPONSE_MOSCOW));
            when(mockApiClient.getAsync(REQUEST_URI_WEATHER_MOSCOW_EN))
                .thenReturn(CompletableFuture.completedFuture(WEATHER_API_RESPONSE_MOSCOW));


            OpenWeatherSDK sdk = new OpenWeatherSDK(API_KEY, UpdateMode.POLLING, LOCALE_EN);
            WeatherData firstCall = sdk.getWeather(NAME_CITY_MOSCOW_RU);
            WeatherData secondCall = sdk.getWeather(NAME_CITY_MOSCOW_EN);

            assertEquals(firstCall, secondCall);

            verify(weatherCache, times(2))
                .getValue(eq(NAME_CITY_MOSCOW_EN));
            verify(weatherCache, times(1))
                .push(eq(NAME_CITY_MOSCOW_EN), any());
        }
    }


    @Test
    void getWeather_ShouldThrowException_WhenCityNotFound() {
        OpenWeatherSDK sdk = new OpenWeatherSDK(API_KEY, UpdateMode.POLLING, LOCALE_EN);

        try (MockedStatic<GeoCodingService> geoCodingServiceMock = mockStatic(GeoCodingService.class)) {

            geoCodingServiceMock.when(() -> GeoCodingService.findCity(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(
                    new UnexpectedErrorException("City not found")));

            assertThrows(UnexpectedErrorException.class, () ->
                sdk.getWeather(NAME_INCORRECT_CITY_2));
        }
    }
}