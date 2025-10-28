package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.repositories.MealRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class MealService {
    private final ItemService itemService;
    private final MealRepository mealRepository;

    public void create(Meal meal)
    {
        mealRepository.save(meal);
    }

    public void update(Meal meal)
    {
        mealRepository.save(meal);
    }

    public Meal findById(long id) {
        return mealRepository.findById(id).orElse(null);
    }


    @GetMapping("/findByDate")
    public List<Meal> findByDate(LocalDate date)
    {
        return mealRepository.findAllByDate(date);
    }
}
