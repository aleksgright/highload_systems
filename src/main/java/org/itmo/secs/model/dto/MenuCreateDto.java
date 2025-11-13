package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Добавляет запись в Menu */
@Getter
@Setter
public class MenuCreateDto {
    private String meal;
    private Long userId;
}
