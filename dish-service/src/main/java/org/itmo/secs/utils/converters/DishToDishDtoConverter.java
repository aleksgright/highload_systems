package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.DishDto;
import org.itmo.secs.model.entities.Dish;
import org.itmo.secs.services.DishService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@AllArgsConstructor
public class DishToDishDtoConverter implements Converter<Dish, DishDto> {
    private DishService dishService;

    @Transactional
    @Override
    public DishDto convert(Dish dish) {
        CCPF ccpf = new CCPF();

        ccpf = Objects.requireNonNull(dishService.makeListOfItems(dish.getId())
                .reduce(ccpf, (acc, curr) -> {
                            acc.setCalories(
                                    (int) (acc.getCalories() + (double) curr.getFirst().getCalories() / 100.0 * curr.getSecond())
                            );
                            acc.setCarbs(
                                    (int) (acc.getCarbs() + (double) curr.getFirst().getCarbs() / 100.0 * curr.getSecond())
                            );
                            acc.setProtein(
                                    (int) (acc.getProtein() + (double) curr.getFirst().getProtein() / 100.0 * curr.getSecond())
                            );
                            acc.setFats(
                                    (int) (acc.getFats() + (double) curr.getFirst().getFats() / 100.0 * curr.getSecond())
                            );

                            return acc;
                }
                ).block()
        );

        return new DishDto(
                dish.getId(), dish.getName(), ccpf.getCalories(), ccpf.getCarbs(), ccpf.getProtein(), ccpf.getFats()
        );
    }
}
