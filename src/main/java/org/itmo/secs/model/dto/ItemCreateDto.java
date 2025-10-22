package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCreateDto {
    private Integer calories;
    private String name;
    private Integer carbs;
    private Integer protein;
    private Integer fats;
}
