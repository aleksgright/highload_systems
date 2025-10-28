package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.MealCreateDto;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.repositories.MealRepository;
import org.itmo.secs.services.MealService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(value = "meal")
public class MealController {
    private final MealService mealService;
    private final ConversionService conversionService;

    @PostMapping("/create")
    public void create(@RequestBody MealCreateDto mealCreateDto) throws ResponseStatusException
    {
//        if (item.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wrong item name");

        mealService.create(conversionService.convert(mealCreateDto, Meal.class));
    }

    @GetMapping("/findByDate")
    public List<Meal> findByDate(Integer year, Integer month, Integer day)
    {
        return mealService.findByDate(LocalDate.of(year, month, day));
    }
}