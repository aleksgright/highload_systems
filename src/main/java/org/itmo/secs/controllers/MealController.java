package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.MealCreateDto;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.repositories.MealRepository;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@AllArgsConstructor
@RestController
@RequestMapping(value = "meal")
public class MealController {
    private final ItemRepository itemRepository;
    private final MealRepository mealRepository;
    private final ConversionService conversionService;

    @PostMapping("/create")
    public void create(@RequestBody MealCreateDto mealCreateDto) throws ResponseStatusException
    {
//        if (item.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wrong item name");

        mealRepository.save(conversionService.convert(mealCreateDto, Meal.class));
    }

    @GetMapping("/findByDate")
    public String findByDate(Integer year, Integer month, Integer day)
    {
        var res = mealRepository.findAllByDate(LocalDate.of(year, month, day));
        if (res.isEmpty()) 
            return "No products";
        var out = "";

        for (var meal : res) {
            out += meal.getItem().getName() + ": " + meal.getCount() 
            + "g; --" + meal.getDate().toString() + "-- " + meal.getTime().toString() + "<br />";
        }

        return out;
    }
}