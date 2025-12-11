package org.itmo.secs.model.dto;

import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.utils.json.LocalDateSerializer;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public record MenuDto(
    Long id,
    @DateTimeFormat(pattern = "yyyy-MM-dd") 
    @JsonSerialize(using = LocalDateSerializer.class)
    LocalDate date,
    @Nullable Long userId,
    String meal
) { }
