package org.itmo.secs.model.dto;

import jakarta.annotation.Nullable;

public record MenuCreateDto(
    String meal,
    @Nullable Long userId
) { }
