package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.MealCreateDto;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.repositories.ItemRepository;
import org.itmo.secs.repositories.MealRepository;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/findByDate")
    public List<Meal> findByDate(LocalDate date)
    {
        return mealRepository.findAllByDate(date);
    }
}
