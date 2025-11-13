package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.ItemDish;
import org.itmo.secs.repositories.DishRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DishService {
    private final ItemDishService itemDishService;
    private final DishRepository dishRepository;
    private final ItemService itemService;

    public void create(Dish dish)
    {
        dishRepository.save(dish);
    }

    public void addItem(long itemId, long dishId, int count) {
        Dish dish = findById(dishId);
        Item item = itemService.findById(itemId);
        if (dish == null) {
            throw new RuntimeException();
        }
        if (item == null) {
            throw new RuntimeException();
        }
        itemDishService.updateItemDishCount(item, dish, count);
        dish.getItems_dishes().add(itemDishService.findById(itemId, dishId));
    }

    public void updateName(Dish dish)
    {
        dishRepository.save(dish);
    }

    public Dish findById(long id) {
        return dishRepository.findById(id).orElse(null);
    }


//    @GetMapping("/findByDate")
//    public List<Dish> findByDate(LocalDate date)
//    {
//        return dishRepository.findAllByDate(date);
//    }
}
