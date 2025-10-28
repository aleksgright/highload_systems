package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealUpdateDto {
    private long id;
    private long itemId;
    private Integer count;
    private String time;
}
