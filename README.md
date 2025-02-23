# OpenWeatherSDK

## Introduction

OpenWeatherSDK is a Java tool designed to interface with the OpenWeatherMap API to retrieve weather information for cities. It simplifies the process of retrieving current weather data for specific cities, offering support for both on-demand and polling modes.
This page describes the most basic configuration, for more detailed information, please see the [Documentation](https://github.com/darkmitya/ow_sdk/wiki/Documentation)

## Contents

- [Installation](#installation)
- [Configuration](#configuration)
- [Usage Example](#usage-example)

## Installation

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>org.openweathermap</groupId>
    <artifactId>openweathermap-sdk</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
## Configuration

To configure and use the OpenWeatherSDK in your application, follow these steps:

1. Obtain an API key from [OpenWeatherMap](https://openweathermap.org/api).
2. Get a new OpenWeatherSDK object using your API key, specifying the update mode and response locale.

```java
OpenWeatherSDK sdk = new OpenWeatherSDK({{API_KEY}}, UpdateMode.POLLING, "en");
```

## Usage Example

```java
// Create an instance of the OpenWeatherSDK
OpenWeatherSDK sdk = new OpenWeatherSDK({{API_KEY}}, UpdateMode.ON_DEMAND, "ru");
WeatherData data = sdk.getWeather("Москва");
System.out.println("Weather in city" + data.getName() + ": "+ data.getWeather().getDescription() + " temp:" + data.getTemperature().getTemp() + "C, ");
```

The JSON object representing weather information can be formatted as follows:

```json
{
  "weather": {
    "main": "Clouds",
    "description": "переменная облачность"
  },
  "temperature": {
    "temp": -7.71,
    "feelsLike": -10.91
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.69
  },
  "datetime": 1750298356,
  "sys": {
    "sunrise": 1740199086,
    "sunset": 1740235699
  },
  "timezone": 10800,
  "name": "Москва"
}
```
