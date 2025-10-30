package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.ZoneId;

import lombok.Getter;
import lombok.Setter;
import org.itmo.secs.model.entities.enums.Meal;

@Entity
@Table(name = "dishes")
@Getter
@Setter
public class Dish {
    public Dish(Long id, @NotNull Item item, @NotNull @PositiveOrZero Integer count, @NotNull LocalDate date,
            long userId, @NotNull Meal meal) {
        this.id = id;
        this.item = item;
        this.count = count;
        this.date = date;
        this.userId = userId;
        this.meal = meal;
    }

    public Dish() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @NotNull
    private Item item;

    @NotNull
    @PositiveOrZero
    private Integer count;

    @NotNull
    private LocalDate date;

    private long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Meal meal;

    @PrePersist
    private void beforeSaving() {
        date = LocalDate.now(ZoneId.of("Europe/Moscow"));
    }
}
