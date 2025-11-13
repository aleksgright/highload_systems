package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "dishes")
@Getter
@Setter
public class Dish {
    public Dish() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "dishes")
    @NotNull
    private List<ItemDish> items_dishes;

    @ManyToMany(mappedBy = "dishes")
    @NotNull
    private List<Menu> menus;
}
