package org.openweathermap.entity;

import lombok.Data;

import java.util.List;

@Data
public class ApiError {
    private int cod;
    private String message;
    private List<String> parameters;
}
