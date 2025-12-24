package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.ItemDish;
import org.itmo.secs.repositories.DishRepository;
import org.itmo.secs.repositories.ItemDishRepository;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
@AllArgsConstructor
public class ItemDishService {
    private final ItemDishRepository itemDishRepository;
    private final ItemService itemService;
    private final DishRepository dishRepository;

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void updateItemDishCount(Item item, Dish dish, int count) {
        Optional<ItemDish> itemDishOpt = itemDishRepository.findById_ItemIdAndId_DishId(item.getId(), dish.getId());
        ItemDish persistUnit = new ItemDish();
        if (itemDishOpt.isEmpty()) {
            persistUnit.setItem(item);
            persistUnit.setDish(dish);
            persistUnit.setCount(count);
        } else {
            persistUnit = itemDishOpt.get();
            persistUnit.setCount(count);
        }
        itemDishRepository.save(persistUnit);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public ItemDish delete(Item item, Dish dish) {
        Optional<ItemDish> itemDishOpt = itemDishRepository.findById_ItemIdAndId_DishId(item.getId(), dish.getId());
        if (itemDishOpt.isEmpty()) {
            throw new ItemNotFoundException("Item with id " + item.getId() + " was not found in Dish with id " + dish.getId());
        }

        itemDishRepository.delete(itemDishOpt.get());
        return itemDishOpt.get();
    }

    public ItemDish findById(long itemId, long dishId) {
        return itemDishRepository.findById_ItemIdAndId_DishId(itemId, dishId).orElse(null);
    }

    public List<ItemDish> findAllByDishId(long dishId) {
        return itemDishRepository.findAllById_DishId(dishId);
    }
}
