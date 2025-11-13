package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Обновляет запись в Dish */
@Getter
@Setter
public class DishUpdateDto {
    private long id;
    private String name;
}
