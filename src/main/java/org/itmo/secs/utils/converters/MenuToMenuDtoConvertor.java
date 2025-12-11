package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.*;
import org.itmo.secs.model.entities.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MenuToMenuDtoConvertor implements Converter<Menu, MenuDto> {
    @Override
    public MenuDto convert(Menu menu) {
        return new MenuDto(menu.getId(), menu.getDate(), (menu.getUser() == null) ? null : menu.getUser().getId(), menu.getMeal().toString());
    }
}
