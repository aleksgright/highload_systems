package org.itmo.secs.utils.converters;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.dto.ItemUpdateDto;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.services.ItemService;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ItemUpdateDtoToItemConverter implements Converter<ItemUpdateDto, Item> {
    private final ItemService itemService;

    @Override
    public Item convert(ItemUpdateDto itemUpdateDto) {
        Item ret = itemService.findById(itemUpdateDto.getId());
        if (ret == null) throw new ItemNotFoundException("Item with this id was not found");
        ret.setId(itemUpdateDto.getId());
        ret.setName(itemUpdateDto.getName());
        ret.setCalories(itemUpdateDto.getCalories());
        ret.setCarbs(itemUpdateDto.getCarbs());
        ret.setProtein(itemUpdateDto.getProtein());
        ret.setFats(itemUpdateDto.getFats());
        return ret;
    }
}
