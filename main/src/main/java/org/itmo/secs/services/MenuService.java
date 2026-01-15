package org.itmo.secs.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.itmo.secs.model.entities.*;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.repositories.MenuRepository;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class MenuService {
    private MenuRepository menuRep;
    private DishService dishService;

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Mono<Menu> save(Menu menu) {
        if (
            menuRep.findByMealAndDateAndUserId(
                menu.getMeal(), 
                menu.getDate(),
                menu.getUser_id()
            ).orElse(null) != null
        ) {
            throw new DataIntegrityViolationException("Menu with given key already exists");
        }

        return Mono.just(menuRep.save(menu));
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void update(Menu menu) {
        if (menuRep.findById(menu.getId()).orElse(null) == null) {
            throw new ItemNotFoundException("Menu with id " + menu.getId() + " was not found");
        }

        Menu foundByKey = menuRep.findByMealAndDateAndUserId(
            menu.getMeal(), 
            menu.getDate(),
            menu.getUser_id()
        ).orElse(null);
        
        if (foundByKey != null && foundByKey.getId() != menu.getId()) {
            throw new DataIntegrityViolationException("Menu with given new key already exists");
        }

        Menu newValue = findById(menu.getId());
        newValue.setMeal(menu.getMeal());
        newValue.setDate(menu.getDate());
        newValue.setUser_id(menu.getUser_id());
        menuRep.save(menu);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void delete(Long id) {
        if (menuRep.findById(id).isEmpty()) {
            throw new ItemNotFoundException("Menu with id " + id.toString() + " was not found");
        }
        menuRep.deleteById(id);
    }

    public Mono<Menu> findById(Long id) {
        Menu menu = menuRep.findById(id).orElse(null);
        return (menu != null) ? Mono.just(menu) : Mono.empty();
    }

    public Mono<Menu> findByKey(Meal meal, LocalDate date, Long userId) {
        Menu menu = menuRep.findByMealAndDateAndUserId(meal, date, userId).orElse(null);
        return (menu != null) ? Mono.just(menu) : Mono.empty();
    }

    public void addDish(long menuId, long dishId) {
        Dish menu = menuRep.findById(menuId).orElse(null);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId + " was not found");
        }

        dishService.findById(dishId)
        .switchIfEmpty(Mono.error(new ItemNotFoundException("Dish with id " + dishId + " was not found")))
        .map((dish) -> {
            dish.getMenus().add(menu);
            menu.getDishes().add(dish);
            dishService.save(menu).subscribe();
            save(dish).subscribe();
            return dish;
        }).subscribe();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void includeDishToMenu(Long dishId, Long menuId) {
        Menu menu = findById(menuId);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found");
        }

        dishService.findById(dishId)
        .switchIfEmpty(Mono.error(new ItemNotFoundException("Dish with id " + dishId.toString() + " was not found")))
        .map((dish) -> {
            if (!menu.getDishes().contains(dish)) {
                menu.getDishes().add(dish);
            }
            menuRep.save(menu);
        }).subscribe();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void deleteDishFromMenu(Long dishId, Long menuId) {
        Menu menu = menuRep.findById(id).orElse(null);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found");
        }

        dishService.findById(dishId)
        .switchIfEmpty(Mono.error(new ItemNotFoundException("Dish with id " + dishId.toString() + " was not found")))
        .map((dish) -> {
            menu.getDishes().remove(dish);
            menuRep.save(menu);
        }).subscribe();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Flux<Dish> makeListOfDishes(Long menuId) {
        Menu menu = menuRep.findById(id).orElse(null);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found");
        }

        return Flux.fromIterable(new ArrayList<>(menu.getDishes()));
    }

    public Flux<Menu> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Flux.fromIterable(menuRep.findAll(pageable).toList());
    }
}
