package org.openweathermap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Instance {
    private UpdateMode mode;
    private String locale;
}
