package org.itmo.secs.services;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.DishRepository;
import org.itmo.secs.utils.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class DishService {
    private final ItemDishService itemDishService;
    private final DishRepository dishRepository;
    private final ItemService itemService;

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Mono<Dish> save(Dish dish) {
        if (dishRepository.findByName(dish.getName()).isEmpty()) {
            throw new DataIntegrityViolationException("Dish with name " + dish.getName() + " already exist");
        }

        return Mono.just(dishRepository.save(dish));
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void addItem(Long itemId, Long dishId, int count) {
        Dish dish = dishRepository.findById(dishId).orElse(null);
        if (dish == null) {
            throw new ItemNotFoundException("Dish with id " + dishId + " was not found");
        }

        itemService.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Item with id " + itemId + " was not found")))
                .subscribe(item -> itemDishService.updateItemDishCount(item, dish, count));
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void deleteItem(Long itemId, Long dishId) {
        Dish dish = dishRepository.findById(dishId).orElse(null);

        if (dish == null) {
            throw new ItemNotFoundException("Dish with id " + itemId + " was not found");
        }

        itemService.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Item with id " + itemId + " was not found")))
                .map(item -> itemDishService.delete(item, dish)).subscribe();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void updateName(Dish dish) {
        findById(dish.getId())
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Dish with id " + dish.getId() + " was not found")))
                .map(x -> dishRepository.save(dish)).subscribe();
    }

    public Mono<Dish> findById(Long id) {
        Dish dish = dishRepository.findById(id).orElse(null);
        return (dish != null) ? Mono.just(dish) : Mono.empty();
    }

    public Mono<Dish> findByName(String name) {
        Dish dish = dishRepository.findByName(name).orElse(null);
        return (dish != null) ? Mono.just(dish) : Mono.empty();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void delete(Long id) {
        if (dishRepository.findById(id).isEmpty()) {
            throw new ItemNotFoundException("Dish with id " + id + " was not found");
        }
        dishRepository.deleteById(id);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Flux<Pair<Item, Integer>> makeListOfItems(Long dishId) {
        return itemDishService.findAllByDishId(dishId)
                .map((itemDish) -> Pair.of(itemDish.getItem(), itemDish.getCount()));
    }

    public Flux<Dish> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Flux.fromIterable(dishRepository.findAll(pageable).toList());
    }
}