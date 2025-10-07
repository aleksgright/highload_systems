package org.itmo.secs.controllers;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.model.entities.enums.MealTime;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.repositories.MealRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

@AllArgsConstructor
@RestController
@RequestMapping(value = "meal")
public class MealController {
    private final ItemRepository itemRepository;
    private final MealRepository mealRepository;

    @PostMapping("/add")
    public void addItem() throws ResponseStatusException
    {
        var item = itemRepository.findByName("Boris");
        if (item.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wrong item name");

        Meal meal = new Meal();
        meal.setItem(item.get());
        meal.setCount(200);
        meal.setDate(LocalDate.now());
        meal.setTime(MealTime.BREAKFAST);
        
        mealRepository.save(meal);
    }

    @GetMapping("/getByDate")
    public String getMealsByDate(Integer year, Integer month, Integer day)
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