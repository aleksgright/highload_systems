package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.MealUpdateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.model.entities.enums.MealTime;
import org.itmo.secs.services.ItemService;
import org.itmo.secs.services.MealService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MealUpdateDtoToMealConverter implements Converter<MealUpdateDto, Meal> {
    private final ItemService itemService;
    private final MealService mealService;

    @Override
    public Meal convert(MealUpdateDto mealUpdateDto) {
        Meal ret = mealService.findById(mealUpdateDto.getId());
        if (ret == null) throw new ItemNotFoundException("Item with this id was not found");
        Item foundItem = itemService.findById(mealUpdateDto.getItemId());
        if (foundItem == null) throw new ItemNotFoundException("Item with this id was not found");
        ret.setId(mealUpdateDto.getId());
        ret.setCount(mealUpdateDto.getCount());
        ret.setTime(MealTime.valueOf(mealUpdateDto.getTime().toUpperCase()));
        ret.setItem(foundItem);
        return ret;
    }
}
