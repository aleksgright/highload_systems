package org.itmo.secs.utils.converters;

import org.itmo.secs.model.dto.ItemDto;
import org.itmo.secs.model.entities.Item;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ItemDtoToItemConverter implements Converter<ItemDto, Item> {

    @Override
    public Item convert(ItemDto itemDto) {
        Item ret = new Item();
        ret.setName(itemDto.getName());
        ret.setCalories(itemDto.getCalories());
        ret.setCarbs(itemDto.getCarbs());
        ret.setProtein(itemDto.getProtein());
        ret.setFats(itemDto.getFats());
        return ret;
    }
}
