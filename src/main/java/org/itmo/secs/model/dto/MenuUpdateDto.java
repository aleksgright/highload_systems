package org.itmo.secs.model.dto;

public record MenuUpdateDto(
    Long id,
    String meal,
    Long userId
) { }
