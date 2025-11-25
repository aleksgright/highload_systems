package org.itmo.secs.model.dto;

record ItemUpdateDto(
    Long id,
    String name,
    Integer calories,
    Integer carbs,
    Integer protein,
    Integer fats
) { }
