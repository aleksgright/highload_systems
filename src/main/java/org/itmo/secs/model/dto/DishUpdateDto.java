package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishUpdateDto {
    private long id;
    private long itemId;
    private Integer count;
    private String meal;
}
