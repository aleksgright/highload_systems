package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishCreateDto {
    private long itemId;
    private Integer count;
    private String meal;
}
