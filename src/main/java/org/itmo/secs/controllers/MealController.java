package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.MealCreateDto;
import org.itmo.secs.model.dto.MealUpdateDto;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.services.MealService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(value = "meal")
public class MealController {
    private final MealService mealService;
    private final ConversionService conversionService;

    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody MealCreateDto mealCreateDto)
    {
        try {
            mealService.create(conversionService.convert(mealCreateDto, Meal.class));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build(); //TODO: NE RABOTAET
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> update(@RequestBody MealUpdateDto mealUpdateDto)
    {
        try {
            mealService.create(conversionService.convert(mealUpdateDto, Meal.class));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build(); //TODO: NE RABOTAET
        }
        return ResponseEntity.ok().build();
    }


    @GetMapping("/findByDate")
    public List<Meal> findByDate(Integer year, Integer month, Integer day)
    {
        return mealService.findByDate(LocalDate.of(year, month, day));
    }
}