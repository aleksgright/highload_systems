package org.itmo.secs.repositories;

import org.itmo.secs.model.entities.ItemDish;

import org.itmo.secs.model.entities.ItemDishId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemDishRepository extends JpaRepository<ItemDish, ItemDishId> {
    Optional<ItemDish> findById_ItemIdAndId_DishId(long itemId, long dishId);
}
