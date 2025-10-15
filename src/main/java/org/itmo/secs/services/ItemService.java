package org.itmo.secs.services;

import lombok.AllArgsConstructor;
import org.itmo.secs.model.entities.Item;
import org.itmo.secs.repositories.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public void saveItem(Item item)
    {
        itemRepository.save(item);
    }

    public Item getItemByName(String name)
    {
        Optional<Item> optionalItem = itemRepository.findByName(name);
        return optionalItem.orElse(null);
    }
}
