package org.itmo.user.accounter.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Объект блюда", description = "Содержит ID и имя продукта из базы")
public record DishDto(
    @Schema(description = "ID продукта", type = "number", example = "1")
    Long id,
    @Schema(description = "Имя продукта", type = "string", example = "Гречка по-купечески")
    String name
) { }
