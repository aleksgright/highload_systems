package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Добавляет объект в списке в Menu */
@Getter
@Setter
public class MenuAddDishDto {
    private Long menuId;
    private Long dishId;
}
