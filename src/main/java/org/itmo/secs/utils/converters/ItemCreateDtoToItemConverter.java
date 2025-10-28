package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.ItemCreateDto;
import org.itmo.secs.model.entities.Item;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ItemCreateDtoToItemConverter implements Converter<ItemCreateDto, Item> {
    @Override
    public Item convert(ItemCreateDto itemCreateDto) {
        Item ret = new Item();
        ret.setName(itemCreateDto.getName());
        ret.setCalories(itemCreateDto.getCalories());
        ret.setCarbs(itemCreateDto.getCarbs());
        ret.setProtein(itemCreateDto.getProtein());
        ret.setFats(itemCreateDto.getFats());
        ret.setCreatorId(1);
        return ret;
    }
}
