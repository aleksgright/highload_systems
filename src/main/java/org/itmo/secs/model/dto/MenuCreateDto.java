package org.itmo.secs.model.dto;

public record MenuCreateDto(
    String meal,
    Long userId
) { }
