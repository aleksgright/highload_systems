package org.itmo.user.accounter.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Объект для создания пользователя", description = "Содержит имя для нового пользователя")
public record UserCreateDto(
    @Schema(description = "Имя пользователя", type = "string", example = "Олег")
    String name
) { }
