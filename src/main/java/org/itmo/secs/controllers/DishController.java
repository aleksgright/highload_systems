package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;

import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.services.DishService;
import org.itmo.secs.services.JsonConvService;
import org.itmo.secs.utils.conf.PagingConf;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping(value = "dish")
public class DishController {
    private final DishService dishService;
    private final ConversionService conversionService;
    private final JsonConvService jsonConvService;
    private final PagingConf pagingConf;

    @PostMapping
    public ResponseEntity<DishDto> create(@RequestBody DishCreateDto dishCreateDto) {
        try {
            return new ResponseEntity<>(
                    conversionService.convert(
                            dishService.create(conversionService.convert(dishCreateDto, Dish.class)),
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
        @RequestParam(name="psize", required=false) Integer pageSize
    ) {
        if (id != null) {
            return findById(id);
        }

        Integer pageNumber;
        if (_pageNumber != null) {
            pageNumber = _pageNumber;
        } else {
            pageNumber = 0;
        }

        if (pageSize == null) {
            return findAll(pageNumber, pagingConf.getDefaultPageSize());
        } else {
            return findAll(
                pageNumber, 
                (pageSize > pagingConf.getMaxPageSize())
                ? pagingConf.getMaxPageSize()
                : pageSize
            );
        }
    }

    public ResponseEntity<String> findById(Long id) {
        Dish dish = dishService.findById(id);
        return (dish != null)
        ? ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(jsonConvService.conv(dish))
        : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> findAll(Integer pageNumber, Integer pageSize) {
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/items")
    public ResponseEntity<Void> addItem(@RequestBody DishAddItemDto dishAddItemDto) {
        dishService.addItem(dishAddItemDto.itemId(), dishAddItemDto.dishId(), dishAddItemDto.count());
        return ResponseEntity.ok().build();
    }
}
