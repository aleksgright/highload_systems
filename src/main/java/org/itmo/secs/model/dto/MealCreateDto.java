package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealCreateDto {
    private long itemId;
    private Integer count;
    private String time;
}
