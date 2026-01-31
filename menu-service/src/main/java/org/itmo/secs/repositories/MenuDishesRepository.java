package org.itmo.secs.repositories;

import org.itmo.secs.model.entities.MenuDishes;
import org.itmo.secs.model.entities.MenuDishesId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MenuDishesRepository extends ReactiveCrudRepository<MenuDishes, MenuDishesId> {
    Flux<MenuDishes> findAllById_MenuId(long menuId);
    Flux<MenuDishes> findAllById_DishId(long dishId);
}
