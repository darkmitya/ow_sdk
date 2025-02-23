package org.openweathermap;


public class CommonConstant {

    public static final Integer MAX_THREADS = 10;
    public static final Integer MAX_CACHE_SIZE = 10;
    public static final long CACHE_TIME_TO_LIVE_MSEC = 10 * 60 * 1000;
    public static final String DEFAULT_LOCALE  = "en";

    public static final String GET_WEATHER_PATH = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&lang=%s&appid=%s";
    public static final String GET_COORDINATES_BY_LOCATION_PATH = "http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&APPID=%s";

}
