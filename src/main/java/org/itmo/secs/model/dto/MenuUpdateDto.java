package org.itmo.secs.model.dto;

record MenuUpdateDto(
    private Long id,
    private String meal,
    private Long userId
) { }
