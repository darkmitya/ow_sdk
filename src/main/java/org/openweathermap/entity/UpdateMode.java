package org.openweathermap.entity;

/**
 * An enumeration representing the mode of operation for the OpenWeatherSDK.
 */
public enum UpdateMode {
    /**
     * Represents on-demand mode, where weather information is retrieved only on demand.
     * This mode caches data for each OpenWeatherSDK instance separately. And periodically refreshes it to be up-to-date.
     * To clear the list of updated cities, use cleanCashedCity()
     */
    ON_DEMAND,

    /**
     * Represents the polling mode where weather information is retrieved for all cached cities.
     * The last 10 requests for all instances OpenWeatherSDK are cached for the city for 10 minutes.
     */
    POLLING
}
