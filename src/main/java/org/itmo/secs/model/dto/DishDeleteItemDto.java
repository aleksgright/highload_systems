package org.itmo.secs.model.dto;

public record DishDeleteItemDto(
    Long dishId,
    Long itemId
) { }
