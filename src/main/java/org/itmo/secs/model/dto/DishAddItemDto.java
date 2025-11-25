package org.itmo.secs.model.dto;

record DishAddItemDto(
    Long itemId,
    Long dishId,
    Integer count,
)
