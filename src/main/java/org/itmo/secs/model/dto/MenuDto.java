package org.itmo.secs.model.dto;
import org.itmo.secs.model.entities.enums.Meal;

import java.time.LocalDate;

public record MenuDto(
    Long id,
    LocalDate date,
    Long user_id,
    Meal meal
) { }
