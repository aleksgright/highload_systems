package org.itmo.secs.model.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.annotation.Nullable;

public record MenuCreateDto(
    String meal,
    @Nullable Long userId,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
) { }
