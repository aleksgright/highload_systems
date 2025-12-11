package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;

import java.util.List;
import org.springframework.data.util.Pair;
import java.util.ArrayList;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.services.*;
import org.itmo.secs.utils.conf.PagingConf;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping(value = "dish")
public class DishController {
    private final DishService dishService;
    private final ItemDishService itemDishService;
    private final ConversionService conversionService;
    private final JsonConvService jsonConvService;
    private final PagingConf pagingConf;

    @PostMapping
    public ResponseEntity<DishDto> create(@RequestBody DishCreateDto dishCreateDto) {
        try {
            return new ResponseEntity<>(
                    conversionService.convert(
                            dishService.save(conversionService.convert(dishCreateDto, Dish.class)),
                            DishDto.class),
                    HttpStatus.CREATED);
        } catch (ConversionException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping
    public ResponseEntity<Void> updateName(@RequestBody DishUpdateNameDto dishUpdateNameDto) {
        try {
            dishService.updateName(conversionService.convert(dishUpdateNameDto, Dish.class));
        } catch (ConversionException e) {
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<String> find(
        @RequestParam(required=false) Long id,
        @RequestParam(name="pnumber", required=false) Integer _pageNumber,
        @RequestParam(name="psize", required=false) Integer _pageSize,
        @RequestParam(required=false) String name
    ) {
        if (id != null && name == null && _pageNumber == null && _pageSize == null) {
            return findById(id);
        } else if (name != null && _pageNumber == null && _pageSize == null) {
            return findByName(name);
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

    public ResponseEntity<String> findById(Long id) {
        Dish dish = dishService.findById(id);
        return (dish != null)
        ? ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(jsonConvService.conv(conversionService.convert(dish, DishDto.class)))
        : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> findByName(String name) {
        Dish dish = dishService.findByName(name);
        return (dish != null)
        ? ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(jsonConvService.conv(conversionService.convert(dish, DishDto.class)))
        : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> findAll(Integer pageNumber, Integer pageSize) {
        List<Dish> dishes = dishService.findAll(pageNumber, pageSize);
        List<DishDto> dishesDto = new ArrayList<>();

        dishes.forEach(
            (Dish d) -> {
                dishesDto.add(conversionService.convert(d, DishDto.class));
            }
        );

        return ResponseEntity.ok().header("Content-Type", "application/json").body(jsonConvService.conv(dishesDto));
    }

    @PutMapping("/items")
    public ResponseEntity<Void> addItem(@RequestBody DishAddItemDto dishAddItemDto) {
        dishService.addItem(dishAddItemDto.itemId(), dishAddItemDto.dishId(), dishAddItemDto.count());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> delete(
        @RequestParam(name="item-id", required=true) Long itemId,
        @RequestParam(name="dish-id", required=true) Long dishId
    ) {
        dishService.deleteItem(itemId, dishId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items")
    public ResponseEntity<String> getItems(@RequestParam(required=true) long id) {
        List<Pair<Item, Integer>> items = dishService.makeListOfItems(id);

        List<ItemCountDto> itemsDto = new ArrayList<>();

        items.forEach((Pair<Item, Integer> it) -> {
            itemsDto.add(new ItemCountDto(conversionService.convert(it.getFirst(), ItemDto.class), it.getSecond()));
        });

        return ResponseEntity.ok().header("Content-Type", "application/json").body(jsonConvService.conv(itemsDto));
    }
}
