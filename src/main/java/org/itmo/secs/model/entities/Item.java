package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {
    public Item() {
    }

    public Item(Long id, @NotNull @PositiveOrZero Integer calories, @NotNull String name,
            @NotNull @PositiveOrZero Integer carbs, @NotNull @PositiveOrZero Integer protein,
            @NotNull @PositiveOrZero Integer fats, long creatorId) {
        this.id = id;
        this.calories = calories;
        this.name = name;
        this.carbs = carbs;
        this.protein = protein;
        this.fats = fats;
        this.creatorId = creatorId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero
    private Integer calories;

    @NotNull
    private String name;

    @NotNull
    @PositiveOrZero
    private Integer carbs;

    @NotNull
    @PositiveOrZero
    private Integer protein;

    @NotNull
    @PositiveOrZero
    private Integer fats;

    private long creatorId;
}
