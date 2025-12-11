package org.itmo.secs.model.dto;

public record ItemCountDto(
    ItemDto item,
    int count
) {}
