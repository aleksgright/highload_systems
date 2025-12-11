package org.itmo.secs.services;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import java.util.List;
import java.util.ArrayList;

import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.ItemDish;
import org.itmo.secs.repositories.DishRepository;
import org.itmo.secs.utils.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DishService {
    private final ItemDishService itemDishService;
    private final DishRepository dishRepository;
    private final ItemService itemService;

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Dish save(Dish dish) {
        if (findByName(dish.getName()) != null) {
            throw new DataIntegrityViolationException("Dish with name " + dish.getName() + " already exist");
        }

        return dishRepository.save(dish);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void addItem(Long itemId, Long dishId, int count) {
        Dish dish = findById(dishId);
        Item item = itemService.findById(itemId);
        if (dish == null) {
            throw new ItemNotFoundException("Dish with id " + dishId + " was not found");
        }
        if (item == null) {
            throw new ItemNotFoundException("Item with id " + itemId + " was not found");
        }
        itemDishService.updateItemDishCount(item, dish, count);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void deleteItem(Long itemId, Long dishId) {
        Dish dish = findById(dishId);
        Item item = itemService.findById(itemId);
        if (dish == null) {
            throw new ItemNotFoundException("Dish with id " + itemId + " was not found");
        }
        if (item == null) {
            throw new ItemNotFoundException("Item with id " + itemId + " was not found");
        }
        itemDishService.delete(item, dish);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void updateName(Dish dish) {
        if (findById(dish.getId()) == null) {
            throw new ItemNotFoundException("Dish with id " + dish.getId() + " was not found");
        }
    
        dishRepository.save(dish);
    }

    public Dish findById(Long id) {
        return dishRepository.findById(id).orElse(null);
    }

    public Dish findByName(String name) {
        return dishRepository.findByName(name).orElse(null);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public List<Pair<Item, Integer>> makeListOfItems(Long dishId) {
        List<ItemDish> itemDishes = itemDishService.findAllByDishId(dishId);
        List<Pair<Item, Integer>> items = new ArrayList<>();

        itemDishes.forEach(
            (ItemDish itemDish) -> {
                items.add(Pair.of(itemDish.getItem(), itemDish.getCount()));
            }
        );

        return items;
    }

    public List<Dish> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return dishRepository.findAll(pageable).toList();
    }
}
