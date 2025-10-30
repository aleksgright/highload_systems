package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.DishCreateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.services.ItemService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DishCreateDtoToDishConverter implements Converter<DishCreateDto, Dish> {
    private final ItemService itemService;

    @Override
    public Dish convert(DishCreateDto dishCreateDto) {
        Item foundItem = itemService.findById(dishCreateDto.getItemId());
        if (foundItem == null)
            throw new ItemNotFoundException("Item with this id was not found");
        Dish ret = new Dish();
        ret.setCount(dishCreateDto.getCount());
        ret.setMeal(Meal.valueOf(dishCreateDto.getMeal().toUpperCase()));
        ret.setItem(foundItem);
        ret.setUserId(1);
        return ret;
    }
}
