package org.itmo.secs.model.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items_dishes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDish {
    @EmbeddedId
    private ItemDishId id;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @MapsId("dishId")
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @NotNull
    private int count;

    public ItemDish(Item item, Dish dish, int count) {
        this.item = item;
        this.dish = dish;
        this.count = count;
    }
}
