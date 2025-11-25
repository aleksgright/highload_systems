package org.itmo.secs.model.dto;

public record ItemCreateDto(
    String name,
    Integer calories,
    Integer carbs,
    Integer protein,
    Integer fats
) { }
