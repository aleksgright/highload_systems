package org.itmo.secs.model.dto;

public record ItemDto(
    Long id,
    String name,
    Integer carbs,
    Integer protein,
    Integer fats,
    Integer calories
) { }
