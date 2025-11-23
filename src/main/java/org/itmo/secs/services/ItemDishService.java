package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.ItemDish;
import org.itmo.secs.model.entities.ItemDishId;
import org.itmo.secs.repositories.DishRepository;
import org.itmo.secs.repositories.ItemDishRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ItemDishService {
    private final ItemDishRepository itemDishRepository;
    private final ItemService itemService;
    private final DishRepository dishRepository;

    public void updateItemDishCount(Item item, Dish dish, int count) {
        Optional<ItemDish> itemDishOpt = itemDishRepository.findById_ItemIdAndId_DishId(item.getId(), dish.getId());
        ItemDish persistUnit = new ItemDish();
        if (itemDishOpt.isEmpty()) {
            persistUnit.setId(new ItemDishId());
            persistUnit.setItem(item);
            persistUnit.setDish(dish);
            persistUnit.setCount(count);
        } else {
            persistUnit = itemDishOpt.get();
            persistUnit.setCount(persistUnit.getCount() + count);
        }
        itemDishRepository.save(persistUnit);

    }

    public ItemDish findById(long itemId, long dishId) {
        return itemDishRepository.findById_ItemIdAndId_DishId(itemId, dishId).orElse(null);
    }


}
