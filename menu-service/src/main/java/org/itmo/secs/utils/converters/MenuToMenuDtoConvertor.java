package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.client.DishServiceClient;
import org.itmo.secs.model.dto.DishDto;
import org.itmo.secs.model.dto.MenuDto;
import org.itmo.secs.model.entities.Menu;
import org.itmo.secs.services.MenuDishesService;
import org.itmo.secs.services.MenuService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class MenuToMenuDtoConvertor implements Converter<Menu, MenuDto> {
    private MenuDishesService menuDishesService;
    private MenuService menuService;
    private DishServiceClient dishService;

    @Override
    public MenuDto convert(Menu menu) {
        CCPF ccpf = new CCPF();

        ccpf = Objects.requireNonNull(menuService.makeListOfDishes(menu.getId())
                .reduce(ccpf, (acc, curr) -> {
                            acc.setCalories(
                                    acc.getCalories() + curr.calories()
                            );
                            acc.setCarbs(
                                    acc.getCarbs() + curr.carbs()
                            );
                            acc.setProtein(
                                    acc.getProtein() + curr.protein()
                            );
                            acc.setFats(
                                    acc.getFats() + curr.fats()
                            );

                            return acc;
                        }
                ).block()
        );

        return new MenuDto(
                menu.getId(),
                menu.getDate(),
                menu.getMeal().toString(),
                ccpf.getCalories(),
                ccpf.getCarbs(),
                ccpf.getProtein(),
                ccpf.getFats()
        );
    }
}
