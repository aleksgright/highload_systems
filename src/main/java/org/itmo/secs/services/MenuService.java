package org.itmo.secs.services;

import java.time.LocalDate;
import java.time.ZoneId;
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

@Service
@AllArgsConstructor
public class MenuService {
    private MenuRepository menuRep;
    private DishService dishService;

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public Menu save(Menu menu) {
        if (
            findByKey(
                menu.getMeal(), 
                menu.getDate(),
                (menu.getUser() == null) ? null : menu.getUser().getId()
            ) != null
        ) {
            throw new DataIntegrityViolationException("Menu with given key already exists");
        }

        return menuRep.save(menu);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void update(Menu menu) {
        if (findById(menu.getId()) == null) {
            throw new ItemNotFoundException("Menu with id " + menu.getId() + " was not found");
        }

        Menu foundByKey = findByKey(
            menu.getMeal(), 
            menu.getDate(),
            (menu.getUser() == null) ? null : menu.getUser().getId()
        );
        
        if (foundByKey != null && foundByKey.getId() != menu.getId()) {
            throw new DataIntegrityViolationException("Menu with given new key already exists");
        }

        Menu newValue = findById(menu.getId());
        newValue.setMeal(menu.getMeal());
        newValue.setDate(menu.getDate());
        newValue.setUser(menu.getUser());
        menuRep.save(menu);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void delete(Long id) {
        if (menuRep.findById(id).isEmpty()) {
            throw new ItemNotFoundException("Menu with id " + id.toString() + " was not found");
        }
        menuRep.deleteById(id);
    }

    public Menu findById(Long id) {
        return menuRep.findById(id).orElse(null);
    }

    public Menu findByKey(Meal meal, LocalDate date, Long userId) {
        return menuRep.findByMealAndDateAndUserId(meal, date, userId).orElse(null);
    }

    public void addDish(long menuId, long dishId) {
        return;
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void includeDishToMenu(Long dishId, Long menuId) {
        Menu menu = findById(menuId);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found");
        }

        Dish dish = dishService.findById(dishId);
        if (dish == null) {
            throw new ItemNotFoundException("Dish with id " + dishId.toString() + " was not found");
        }

        if (!menu.getDishes().contains(dish)) {
            menu.getDishes().add(dish);
        }

        menuRep.save(menu);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void deleteDishFromMenu(Long dishId, Long menuId) {
        Menu menu = findById(menuId);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found");
        }

        Dish dish = dishService.findById(dishId);
        if (dish == null) {
            throw new ItemNotFoundException("Dish with id " + dishId.toString() + " was not found");
        }

        menu.getDishes().remove(dish);

        menuRep.save(menu);
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public List<Dish> makeListOfDishes(Long menuId) {
        Menu menu = findById(menuId);
        if (menu == null) {
            throw new ItemNotFoundException("Menu with id " + menuId.toString() + " was not found");
        }

        return new ArrayList<>(menu.getDishes());
    }

    public List<Menu> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return menuRep.findAll(pageable).toList();
    }
}
