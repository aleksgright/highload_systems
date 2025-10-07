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
}
