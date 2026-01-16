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
        Item ret = itemService.findById(itemUpdateDto.id())
                .blockOptional()
                .orElseThrow(() ->
                        new ItemNotFoundException("Item with id " + itemUpdateDto.id() + " was not found"));

        ret.setName(itemUpdateDto.name());
        ret.setCalories(itemUpdateDto.calories());
        ret.setCarbs(itemUpdateDto.carbs());
        ret.setProtein(itemUpdateDto.protein());
        ret.setFats(itemUpdateDto.fats());

        return ret;
    }
}
