package org.itmo.secs.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.enums.*;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.model.entities.User;
import org.itmo.secs.services.*;
import org.itmo.secs.utils.conf.PagingConf;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "menu")
public class MenuController {
    private MenuService menuService;
    private UserService userService;
    private ConversionService conversionService;
    private final JsonConvService jsonConvService;
    private final PagingConf pagingConf;

    @PostMapping
    public ResponseEntity<MenuDto> create(@RequestBody MenuCreateDto menuDto) {
        Menu menu = conversionService.convert(menuDto, Menu.class);
        return new ResponseEntity<>(
                conversionService.convert(
                        menuService.save(menu),
                        MenuDto.class),
                HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<MenuDto> update(@RequestBody MenuDto menuDto) {
        Menu menu = conversionService.convert(menuDto, Menu.class);
        menuService.update(menu);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam(name="id", required=true) Long menuId) {
        menuService.delete(menuId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<String> find(
        @RequestParam(required=false) Long id,
        @RequestParam(name="pnumber", required=false) Integer _pageNumber,
        @RequestParam(name="psize", required=false) Integer _pageSize
    ) {
        if (id != null) {
            return findById(id);
        } else {
            Integer pageNumber = (_pageNumber == null) ? 0 : _pageNumber;
            Integer pageSize = (_pageSize == null) 
                ? pagingConf.getDefaultPageSize()
                : (_pageSize > pagingConf.getMaxPageSize())
                    ? pagingConf.getMaxPageSize()
                    : _pageSize;
            return findAll(pageNumber, pageSize);
        }
    }

    public ResponseEntity<String> findAll(Integer pageNumber, Integer pageSize) {
        List<Menu> menus = menuService.findAll(pageNumber, pageSize);
        List<MenuDto> menusDto = new ArrayList<>();
        menus.forEach((Menu it) -> { 
            menusDto.add(conversionService.convert(it, MenuDto.class));
        });
        return ResponseEntity.ok(jsonConvService.conv(menusDto));
    }

    public ResponseEntity<String> findById(Long id) {
        Menu menu = menuService.findById(id);
        if (menu != null) {
            System.out.println(jsonConvService.conv(
                conversionService.convert(menu, MenuDto.class)
            ));
        }
        return (menu == null) 
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(jsonConvService.conv(
                conversionService.convert(menu, MenuDto.class)
            ));
    }

    @PutMapping("/dishes")
    public ResponseEntity<Void> addDish(@RequestBody MenuDishDto dto) {
        menuService.includeDishToMenu(dto.dishId(), dto.menuId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dishes")
    public ResponseEntity<String> getDishes(@RequestParam(required=true) Long id) {
        List<Dish> dishes = menuService.makeListOfDishes(id);

        List<DishDto> dishesDto = new ArrayList<>();

        dishes.forEach((Dish it) -> {
            dishesDto.add(conversionService.convert(it, DishDto.class));
        });

        return ResponseEntity.ok(jsonConvService.conv(dishesDto));
    }

    @DeleteMapping("/dishes")
    public ResponseEntity<Void> deleteDish(@RequestBody MenuDishDto dto) {
        menuService.deleteDishFromMenu(dto.dishId(), dto.menuId());
        return ResponseEntity.noContent().build();
    }
}
