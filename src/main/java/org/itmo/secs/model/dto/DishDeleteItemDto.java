package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Удаляет запись в ItemDish */
@Getter
@Setter
public class DishDeleteItemDto {
    private long dishId;
    private long itemId;
}
