package org.openweathermap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeatherData {
    private WeatherInfo weather;
    private TemperatureInfo temperature;
    private Long visibility;
    private WindInfo wind;
    private Long datetime;
    private SysInfo sys;
    private Integer timezone;
    private String name;
}
