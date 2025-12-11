package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @NotNull
    private List<ItemDish> items_dishes = new ArrayList<>();

    @ManyToMany(mappedBy = "dishes", cascade = CascadeType.REMOVE)
    @NotNull
    private List<Menu> menus = new ArrayList<>();

    public Dish (String name, List<ItemDish> items_dishes, List<Menu> menus) {
        this.name = name;
        this.items_dishes = items_dishes;
        this.menus = menus;
    }
}
