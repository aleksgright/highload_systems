package org.itmo.secs.utils.converters;

import org.itmo.secs.model.dto.DishCreateDto;
import org.itmo.secs.model.dto.DishResponseDto;
import org.itmo.secs.model.entities.Dish;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DishToDishResponseDtoConverter implements Converter<Dish, DishResponseDto> {
    @Override
    public DishResponseDto convert(Dish dish) {
        return new DishResponseDto(
            dish.getId(),
            dish.getName()
        );
    }
}
