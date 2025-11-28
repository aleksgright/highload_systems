package org.itmo.secs.utils.converters;

import org.itmo.secs.model.dto.DishDto;
import org.itmo.secs.model.entities.Dish;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DishToDishDtoConverter implements Converter<Dish, DishDto> {
    @Override
    public DishDto convert(Dish dish) {
        return new DishDto(
            dish.getId(),
            dish.getName()
        );
    }
}
