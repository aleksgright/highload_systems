package org.itmo.secs.model.dto;

import java.time.LocalDate;
import java.util.List;

public record MenuResponseDto(
        Long id,
        LocalDate date,
        Long userId,
        String meal,
        List<DishResponseDto> dishes
) { }