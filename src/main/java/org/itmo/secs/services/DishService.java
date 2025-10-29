package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.repositories.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class DishService {
    private final ItemService itemService;
    private final DishRepository dishRepository;

    public void create(Dish dish)
    {
        dishRepository.save(dish);
    }

    public void update(Dish dish)
    {
        dishRepository.save(dish);
    }

    public Dish findById(long id) {
        return dishRepository.findById(id).orElse(null);
    }


    @GetMapping("/findByDate")
    public List<Dish> findByDate(LocalDate date)
    {
        return dishRepository.findAllByDate(date);
    }
}
