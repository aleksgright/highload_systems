package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.MenuDishes;
import org.itmo.secs.model.entities.MenuDishesId;
import org.itmo.secs.repositories.MenuDishesRepository;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class MenuDishesService {
    private MenuDishesRepository menuDishesRep;

    public Mono<MenuDishes> saveById(MenuDishesId id) {
        return menuDishesRep.findById(id)
                .doOnNext(x -> {
                    throw new DataIntegrityViolationException(
                            "Dish with id " + id.dishId() + " already in menu with id " + id.menuId()
                    );
                })
                .switchIfEmpty(menuDishesRep.save(new MenuDishes(id)));
    }

    public Mono<Void> deleteById(MenuDishesId id) {
        return menuDishesRep.findById(id)
                .switchIfEmpty(Mono.error(
                    new DataIntegrityViolationException(
                            "Dish with id " + id.dishId() + " already in menu with id " + id.menuId()
                    ))
                )
                .flatMap(x -> menuDishesRep.deleteById(id));
    }

    public Flux<Long> getDishesIdByMenuId(Long menuId) {
        return menuDishesRep.findAllById_MenuId(menuId)
                .map(id -> id.getId().dishId());
    }
}
