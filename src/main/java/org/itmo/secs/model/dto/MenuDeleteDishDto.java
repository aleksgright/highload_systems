package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

/* Удаляет объект из списка в Menu */
@Getter
@Setter
public class MenuDeleteDishDto {
    private Long menuId;
    private Long dishId;
}
