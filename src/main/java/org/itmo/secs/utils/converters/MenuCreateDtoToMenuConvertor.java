package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.*;
import org.itmo.secs.model.entities.enums.Meal;
import org.itmo.secs.services.UserService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MenuCreateDtoToMenuConvertor implements Converter<MenuCreateDto, Menu> {
    private final UserService userService;

    @Override
    public Menu convert(MenuCreateDto menuDto) {
        Menu menu = new Menu();
        User user = null;
        if (menuDto.userId() != null) {
            user = userService.findById(menuDto.userId());
            if (user == null) {
                throw new ItemNotFoundException("User with id " + String.valueOf(menuDto.userId()) + " was not found");
            }
        }

        menu.setUser(user);
        menu.setMeal(Meal.valueOf(menuDto.meal()));
        return menu;
    }
}
