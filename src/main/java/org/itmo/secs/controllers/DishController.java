package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.DishCreateDto;
import org.itmo.secs.model.dto.DishUpdateDto;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.services.DishService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(value = "dish")
public class DishController {
    private final DishService dishService;
    private final ConversionService conversionService;

    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody DishCreateDto dishCreateDto)
    {
        try {
            dishService.create(conversionService.convert(dishCreateDto, Dish.class));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build(); //TODO: NE RABOTAET
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> update(@RequestBody DishUpdateDto dishUpdateDto)
    {
        try {
            dishService.create(conversionService.convert(dishUpdateDto, Dish.class));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build(); //TODO: NE RABOTAET
        }
        return ResponseEntity.ok().build();
    }


    @GetMapping("/findByDate")
    public List<Dish> findByDate(Integer year, Integer month, Integer day)
    {
        return dishService.findByDate(LocalDate.of(year, month, day));
    }
}