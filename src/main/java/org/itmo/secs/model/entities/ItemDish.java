package org.itmo.secs.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
    @JsonIgnore
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JsonIgnore
    @MapsId("dishId")
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @NotNull
    private int count;
}
