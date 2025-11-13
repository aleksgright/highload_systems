package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Обновляет запись в Menu */
@Getter
@Setter
public class MenuUpdateDto {
    private Long id;
    private String meal;
    private Long userId;
}
