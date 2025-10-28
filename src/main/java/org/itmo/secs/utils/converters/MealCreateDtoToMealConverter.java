package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.MealCreateDto;
import org.itmo.secs.model.entities.Meal;
import org.itmo.secs.model.entities.enums.MealTime;
import org.itmo.secs.services.ItemService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MealCreateDtoToMealConverter implements Converter<MealCreateDto, Meal> {
    private final ItemService itemService;

    @Override
    public Meal convert(MealCreateDto mealCreateDto) {
        Meal ret = new Meal();
        ret.setCount(mealCreateDto.getCount());
        ret.setTime(MealTime.valueOf(mealCreateDto.getTime().toUpperCase()));
        ret.setItem(itemService.findById(mealCreateDto.getItemId()));
        ret.setUserId(1);
        return ret;
    }
}
