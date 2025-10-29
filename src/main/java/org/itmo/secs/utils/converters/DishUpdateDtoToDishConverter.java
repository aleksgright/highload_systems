package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.DishUpdateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.services.ItemService;
import org.itmo.secs.services.DishService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DishUpdateDtoToDishConverter implements Converter<DishUpdateDto, Dish> {
    private final ItemService itemService;
    private final DishService dishService;

    @Override
    public Dish convert(DishUpdateDto dishUpdateDto) {
        Dish ret = dishService.findById(dishUpdateDto.getId());
        if (ret == null) throw new ItemNotFoundException("Item with this id was not found");
        Item foundItem = itemService.findById(dishUpdateDto.getItemId());
        if (foundItem == null) throw new ItemNotFoundException("Item with this id was not found");
        ret.setId(dishUpdateDto.getId());
        ret.setCount(dishUpdateDto.getCount());
        ret.setMeal(Meal.valueOf(dishUpdateDto.getMeal().toUpperCase()));
        ret.setItem(foundItem);
        return ret;
    }
}
