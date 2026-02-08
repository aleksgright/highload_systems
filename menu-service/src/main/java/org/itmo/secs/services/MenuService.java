package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.client.DishServiceClient;
import org.itmo.secs.client.UserServiceClient;
import org.itmo.secs.model.dto.DishDto;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.repositories.MenuRepository;
import org.itmo.secs.utils.exceptions.DataIntegrityViolationException;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.itmo.secs.utils.exceptions.ServiceUnavailableException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Objects;

@Service
@AllArgsConstructor
public class MenuService {
    private MenuRepository menuRep;
    private MenuDishesService menuDishesService;
    private DishServiceClient dishServiceClient;
    private UserServiceClient userServiceClient;

    public Mono<Menu> save(Menu menu) {
        return userServiceClient
                .getById(menu.getUserId())
                .onErrorResume(Mono::error)
                .hasElement()
                .flatMap(_x ->
                    menuRep.findByMealAndDateAndUserId(
                        menu.getMeal(),
                        menu.getDate(),
                        menu.getUserId()
                    )
                            .doOnNext(x -> {
                                throw new DataIntegrityViolationException("Menu with given key already exists");
                            })
                            .switchIfEmpty(menuRep.save(menu))
                );
    }

    public Mono<Void> update(Menu menu) {
        return findById(menu.getId())
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Menu with id " + menu.getId() + " was not found")))
                .flatMap(existingMenu -> findByKey(menu.getMeal(), menu.getDate(), menu.getUserId())
                    .flatMap(foundMenu -> {
                        if (!Objects.equals(foundMenu.getId(), menu.getId())) {
                            return Mono.error(new DataIntegrityViolationException("Menu with given new key already exists"));
                        } else {
                            return Mono.just(existingMenu);
                        }
                    })
                    .switchIfEmpty(Mono.just(existingMenu))
                )
                .flatMap(existingMenu -> {
                    existingMenu.setMeal(menu.getMeal());
                    existingMenu.setDate(menu.getDate());
                    existingMenu.setUserId(menu.getUserId());
                    return menuRep.save(existingMenu);
                })
                .then();
    }

    public Mono<Void> delete(Long id) {
        return menuRep.findById(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Menu with id " + id + " was not found")))
                .flatMap(x -> menuRep.deleteById(id)).then();
    }

    public Mono<Menu> findById(Long id) {
       return menuRep.findById(id);
    }

    public Mono<Menu> findByKey(Meal meal, LocalDate date, Long userId) {
        return  menuRep.findByMealAndDateAndUserId(meal, date, userId);
    }

    public Mono<Void> includeDishToMenu(Long dishId, Long menuId) {
        return menuRep.findById(menuId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Menu with id " + menuId + " was not found")))
                .flatMap(menu -> menuDishesService.getDishesIdByMenuId(menuId)
                        .any(dishId::equals)
                        .flatMap(res -> {
                            if (res) {
                                return Mono.error(new DataIntegrityViolationException("Dish with id " + dishId + " already in menu with id " + menuId));
                            } else {
                                return Mono.just(menu);
                            }
                        })
                )
                .flatMap(menu -> dishServiceClient.getById(dishId)
                        .onErrorResume(Mono::error)
                        .flatMap((dish) -> menuDishesService.saveByIds(menuId, dishId))
                )
                .then();
    }

    public Mono<Void> deleteDishFromMenu(Long dishId, Long menuId) {
        return menuRep.findById(menuId)
                .switchIfEmpty(Mono.error(
                        new ItemNotFoundException("Menu with id " + menuId + " was not found")
                ))
                .flatMap(menu ->
                        menuDishesService.getDishesIdByMenuId(menuId)
                                .any(dishId::equals)
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return Mono.error(new ItemNotFoundException(
                                                "Dish with id " + dishId + " is not in menu with id " + menuId));
                                    }
                                    return menuDishesService.deleteByIds(menuId, dishId);
                                })
                )
                .then();
    }

    public Flux<DishDto> makeListOfDishes(Long menuId) {
        return menuRep.findById(menuId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Menu with id " + menuId + " was not found")))
                .flatMapMany(x -> menuDishesService.getDishesIdByMenuId(x.getId()))
                .flatMap(dishId -> dishServiceClient.getById(dishId)
                        .onErrorResume(e -> {
                            if (e instanceof ServiceUnavailableException) {
                                return Mono.error(e);
                            } else {
                                return Mono.just(new DishDto(dishId, "(not found)", 0, 0, 0, 0));
                            }
                        }));
    }

    public Flux<Menu> findAll(int page, int size) {
        return menuRep.findAll().skip((long) page * size).limitRate(size);
    }

    public Flux<Menu> findAllByUsername(String username) {
        return userServiceClient.getByName(username)
                .onErrorResume(Mono::error)
                .flatMapMany(user -> menuRep.findAllByUserId(user.id()));
    }
}
