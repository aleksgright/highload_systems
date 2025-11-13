package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.DishAddItemDto;
import org.itmo.secs.model.dto.DishCreateDto;
import org.itmo.secs.model.dto.DishUpdateNameDto;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.services.DishService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
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

    @PutMapping("/updateName")
    public ResponseEntity<Void> updateName(@RequestBody DishUpdateNameDto dishUpdateNameDto)
    {
        try {
            dishService.updateName(conversionService.convert(dishUpdateNameDto, Dish.class));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build(); //TODO: NE RABOTAET
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/addItem")
    public ResponseEntity<Void> addItem(@RequestBody DishAddItemDto dishAddItemDto)
    {
        dishService.addItem(dishAddItemDto.getItemId(), dishAddItemDto.getDishId(), dishAddItemDto.getCount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findById")
    public ResponseEntity<Dish> findById(@RequestParam long id)
    {
        Dish dish = dishService.findById(id);
        return dish != null ? new ResponseEntity<>(dish, HttpStatus.OK) : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }



//    @GetMapping("/findByDate")
//    public List<Dish> findByDate(Integer year, Integer month, Integer day)
//    {
//        return dishService.findByDate(LocalDate.of(year, month, day));
//    }
}