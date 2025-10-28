package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.ZoneId;

import lombok.Getter;
import lombok.Setter;
import org.itmo.secs.model.entities.enums.MealTime;

@Entity
@Table(name = "meals")
@Getter
@Setter
public class Meal {
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
    private MealTime time;

    @PrePersist
    private void beforeSaving() {
        date = LocalDate.now(ZoneId.of("Europe/Moscow"));
    }
}
