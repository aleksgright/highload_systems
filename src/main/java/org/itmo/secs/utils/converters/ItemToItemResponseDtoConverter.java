package org.itmo.secs.utils.converters;

import org.itmo.secs.model.dto.ItemResponseDto;
import org.itmo.secs.model.entities.Item;
import org.springframework.core.convert.converter.Converter;

public class ItemToItemResponseDtoConverter implements Converter<Item, ItemResponseDto> {
    @Override
    public ItemResponseDto convert(Item item) {
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getCalories(),
                item.getCarbs(),
                item.getProtein(),
                item.getFats()
        );
    }
}
