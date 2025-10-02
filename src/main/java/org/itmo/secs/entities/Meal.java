package org.itmo.secs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.itmo.secs.entities.Item;
import org.itmo.secs.entities.enums.MealTime;

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
    private Item item;

    @NotNull
    @PositiveOrZero
    private Integer count;

    @NotNull
    private LocalDate date;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MealTime time;
}
