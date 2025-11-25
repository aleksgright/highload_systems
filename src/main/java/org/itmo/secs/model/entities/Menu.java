package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.itmo.secs.model.entities.enums.Meal;

@Entity
@Table(name = "menus", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueDateMealUser", columnNames = { "date", "meal", "user_id" })
})
@Getter
@Setter
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @NotNull
    @JoinTable(
        name = "menu_dishes", 
        joinColumns = @JoinColumn(name = "menu_id", referencedColumnName = "id"), 
        inverseJoinColumns = @JoinColumn(name = "dish_id", referencedColumnName = "id")
    )
    private List<Dish> dishes = new ArrayList<>();

    @NotNull
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Meal meal;

    @PrePersist
    private void beforeSaving() {
        date = LocalDate.now(ZoneId.of("Europe/Moscow"));
    }
}
