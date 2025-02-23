package org.openweathermap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openweathermap.entity.WeatherData;

import java.net.URI;

public class TestObjectFactory {

    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static final String API_KEY = "7a4295b0d73adcc1dce231fdd412fb40";

    public static final String NAME_INCORRECT_CITY_2 = "PPPPPP";

    public static final String LOCALE_EN = "en";
    public static final String LOCALE_RU = "ru";

    public static final String NAME_CITY_MOSCOW_RU = "Москва";
    public static final String NAME_CITY_MOSCOW_EN = "Moscow";

    public static final URI REQUEST_URI_CITY_MOSCOW_EN = URI.create("http://api.openweathermap.org/geo/1.0/direct?q=Moscow&limit=1&APPID=" + API_KEY);
    public static final URI REQUEST_URI_CITY_MOSCOW_RU = URI.create("http://api.openweathermap.org/geo/1.0/direct?q=Москва&limit=1&APPID=" + API_KEY);
    public static final URI REQUEST_URI_WEATHER_MOSCOW_EN = URI.create("https://api.openweathermap.org/data/2.5/weather?lat=55.7504461&lon=37.6174943&units=metric&lang=en&appid=" + API_KEY);
    public static final URI REQUEST_URI_WEATHER_MOSCOW_RU = URI.create("https://api.openweathermap.org/data/2.5/weather?lat=55.7504461&lon=37.6174943&units=metric&lang=ru&appid=" + API_KEY);
    public static final WeatherData WEATHER_RESPONSE_MOSCOW;

    public static final String CITY_API_RESPONSE_MOSCOW = """
        [
            {
                "name": "Moscow",
                "local_names":
                {
                    "fi": "Moskova",
                    "ru": "Москва",
                    "yo": "Mọsko",
                    "wa": "Moscou",
                    "hu": "Moszkva",
                    "an": "Moscú",
                    "sc": "Mosca",
                    "te": "మాస్కో",
                    "ko": "모스크바",
                    "vi": "Mát-xcơ-va",
                    "bi": "Moskow",
                    "en": "Moscow"
                },
                "lat": 55.7504461,
                "lon": 37.6174943,
                "country": "RU",
                "state": "Moscow"
            }
        ]""";

    public static final String WEATHER_API_RESPONSE_MOSCOW = """
        {"coord":
        {
            "lon": 37.6175,
            "lat": 55.7504
        },
        "weather":
        [
            {
                "id": 802,
                "main": "Clouds",
                "description": "переменная облачность",
                "icon": "03d"
            }
        ],
        "base": "stations",
        "main":
        {
            "temp": -7.71,
            "feels_like": -10.91,
            "temp_min": -7.71,
            "temp_max": -6.66,
            "pressure": 1036,
            "humidity": 84,
            "sea_level": 1036,
            "grnd_level": 1015
        },
        "visibility": 10000,
        "wind":
        {
            "speed": 1.69,
            "deg": 111,
            "gust": 2.58
        },
        "clouds":
        {
            "all": 29
        },
        "dt": 1750298356,
        "sys":
        {
            "type": 1,
            "id": 9027,
            "country": "RU",
            "sunrise": 1740199086,
            "sunset": 1740235699
        },
        "timezone": 10800,
        "id": 524901,
        "name": "Москва",
        "cod": 200
    }""";

    static {
        try {
            WEATHER_RESPONSE_MOSCOW = JACKSON_MAPPER.readValue(
                """
                    {
                        "weather":
                        {
                            "main": "Clouds",
                            "description": "переменная облачность"
                        },
                        "temperature":
                        {
                            "temp": -7.71,
                            "feelsLike": -10.91
                        },
                        "visibility": 10000,
                        "wind":
                        {
                            "speed": 1.69
                        },
                        "datetime": 1750298356,
                        "sys":
                        {
                            "sunrise": 1740199086,
                            "sunset": 1740235699
                        },
                        "timezone": 10800,
                        "name": "Москва"
                    }""", WeatherData.class);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
