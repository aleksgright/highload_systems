package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Обновляет запись в Dish */
@Getter
@Setter
public class DishUpdateNameDto {
    private long id;
    private String name;
}
