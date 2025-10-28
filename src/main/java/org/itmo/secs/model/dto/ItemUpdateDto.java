package org.itmo.secs.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemUpdateDto {
    private Long id;
    private Integer calories;
    private String name;
    private Integer carbs;
    private Integer protein;
    private Integer fats;
}
