package org.itmo.secs.model.dto;

public record ItemResponseDto (
    Long id,
    String name,
    Integer calories,
    Integer carbs,
    Integer protein,
    Integer fats
){ }
