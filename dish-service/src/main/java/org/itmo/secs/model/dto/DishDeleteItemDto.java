package org.itmo.secs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Объект для удаления продукта из состава блюда", description = "Содержит ID соответствующих продукта и блюда")
public record DishDeleteItemDto(
    @Schema(description = "ID блюда", type = "number", example = "1")
    Long dishId,
    @Schema(description = "ID продукта", type = "number", example = "2")
    Long itemId
) { }
