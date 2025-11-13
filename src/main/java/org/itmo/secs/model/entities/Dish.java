package org.itmo.secs.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

    @OneToMany
    @NotNull
    @ElementCollection(targetClass = ItemDish.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "items_dishes", joinColumns = @JoinColumn(name = "id"))
    private List<ItemDish> items_dishes = new ArrayList<>();

    @ManyToMany
    @NotNull
    @ElementCollection(targetClass = Menu.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "menus", joinColumns = @JoinColumn(name = "id"))
    private List<Menu> menus = new ArrayList<>();
}
