package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Добавляет запись в ItemDish */
@Getter
@Setter
public class DishAddItemDto {
    private long itemId;
    private long dishId;
    private int count;
}
