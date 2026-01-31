package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.itmo.secs.model.entities.enums.Meal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(cascade = CascadeType.PERSIST)
    @NotNull
    private List<Long> dishes_id = new ArrayList<>();

    @NotNull
    private LocalDate date;

    @NotNull
    private Long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Meal meal;
}
