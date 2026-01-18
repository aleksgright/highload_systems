package org.itmo.secs.services;

import java.time.LocalDate;
import java.util.ArrayList;

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
                menu.getUserId()
            ).orElse(null) != null
        ) {
            throw new DataIntegrityViolationException("Menu with given key already exists");
        }

        return Mono.just(menuRep.save(menu));
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void update(Menu menu) {
        findById(menu.getId())
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Menu with id " + menu.getId() + " was not found")))
                .flatMap(existingMenu -> {
                    return findByKey(menu.getMeal(), menu.getDate(), menu.getUserId())
                            .flatMap(foundMenu -> {
                                if (foundMenu.getId() != menu.getId()) {
                                    return Mono.error(new DataIntegrityViolationException("Menu with given new key already exists"));
                                } else {
                                    return Mono.just(existingMenu);
                                }
                            })
                            .switchIfEmpty(Mono.just(existingMenu));
                })
                .flatMap(existingMenu -> {
                    existingMenu.setMeal(menu.getMeal());
                    existingMenu.setDate(menu.getDate());
                    existingMenu.setUserId(menu.getUserId());
                    return Mono.fromCallable(() -> menuRep.save(existingMenu));
                })
                .then();
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
        Menu menu = menuRep.findById(menuId).orElse(null);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId + " was not found");
        }

        dishService.findById(dishId)
        .switchIfEmpty(Mono.error(new ItemNotFoundException("Dish with id " + dishId + " was not found")))
        .map((dish) -> {
            dish.getMenus().add(menu);
            menu.getDishes().add(dish);
            dishService.save(dish).subscribe();
            save(menu).subscribe();
            return dish;
        }).subscribe();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void includeDishToMenu(Long dishId, Long menuId) {
        findById(menuId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found")))
                .flatMap(menu -> dishService.findById(dishId)
                        .switchIfEmpty(Mono.error(new ItemNotFoundException("Dish with id " + dishId.toString() + " was not found")))
                        .flatMap(dish -> {
                            if (!menu.getDishes().contains(dish)) {
                                menu.getDishes().add(dish);
                            }
                            return Mono.fromCallable(() -> menuRep.save(menu));
                        }))
                .then();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void deleteDishFromMenu(Long dishId, Long menuId) {
        Mono.fromCallable(() -> {
            Menu menu = menuRep.findById(menuId)
                    .orElseThrow(() -> new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found"));

            Dish dish = dishService.findById(dishId)
                    .blockOptional()
                    .orElseThrow(() -> new ItemNotFoundException("Dish with id " + dishId.toString() + " was not found"));

            menu.getDishes().remove(dish);
            menuRep.save(menu);
            return null;
        }).then();
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Flux<Dish> makeListOfDishes(Long menuId) {
        Menu menu = menuRep.findById(menuId).orElse(null);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId + " was not found");
        }

        return Flux.fromIterable(new ArrayList<>(menu.getDishes()));
    }

    public Flux<Menu> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Flux.fromIterable(menuRep.findAll(pageable).toList());
    }
}
