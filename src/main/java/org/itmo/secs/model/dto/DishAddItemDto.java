package org.itmo.secs.model.dto;

public record DishAddItemDto(
    Long itemId,
    Long dishId,
    Integer count
) { }
