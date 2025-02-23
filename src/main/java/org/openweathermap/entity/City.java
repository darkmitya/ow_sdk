package org.openweathermap.entity;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class City {
    private String name;
    private String localeName;
    private String country;
    private String state;
    private double lat;
    private double lon;
}
