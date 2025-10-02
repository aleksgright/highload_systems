package org.itmo.secs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    private Integer calories;

    @NotNull
    private String name;

    @NotNull
    private Integer carbs;

    @NotNull
    private Integer protein;

    @NotNull
    private Integer fats;
}
