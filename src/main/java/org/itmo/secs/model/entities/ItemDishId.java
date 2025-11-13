package org.itmo.secs.model.entities;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ItemDishId implements Serializable {
    private Long itemId;
    private Long dishId;
}